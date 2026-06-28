package fr.junade.gainznote

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.android.billingclient.api.PendingPurchasesParams
import kotlinx.coroutines.*

/**
 * Gère l'achat in-app "gainznote_remove_ads" via Google Play Billing.
 *
 * Utilisation :
 *  - Appeler [startConnection] au démarrage (onCreate).
 *  - Appeler [launchPurchase] pour déclencher le flow d'achat.
 *  - Lire [isAdFree] pour savoir si l'utilisateur a acheté.
 *  - [onAdFreeChanged] est appelé quand l'état change (pour mettre à jour l'UI).
 *  - [onPurchaseJustCompleted] est appelé uniquement quand un NOUVEL achat vient
 *    d'être validé par l'utilisateur (flow d'achat en direct), pour afficher une
 *    confirmation. Il n'est jamais déclenché lors des détections passives
 *    (démarrage, onResume, restauration) afin d'éviter une pop-up hors contexte.
 *  - [onPriceChanged] est appelé avec le prix formaté récupéré depuis le Play Store.
 */
class BillingManager(
    context: Context,
    private val onAdFreeChanged: (Boolean) -> Unit,
    private val onPurchaseJustCompleted: (() -> Unit)? = null,
    private val onPriceChanged: ((String) -> Unit)? = null
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "GainzBilling"
        const val PRODUCT_ID = "gainznote_remove_ads"
    }

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    private var productDetails: ProductDetails? = null
    var isAdFree: Boolean = false
        private set

    /** Connecte le BillingClient et vérifie les achats existants. */
    fun startConnection() {
        try {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Billing connecté")
                        queryProduct()
                        queryExistingPurchases()
                    } else {
                        Log.w(TAG, "Billing setup failed: ${result.debugMessage}")
                    }
                }
                override fun onBillingServiceDisconnected() {
                    Log.w(TAG, "Billing déconnecté")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Billing startConnection exception: ${e.message}", e)
        }
    }

    /** Récupère les détails du produit depuis le Play Store (prix localisé inclus). */
    private fun queryProduct() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            ).build()

        billingClient.queryProductDetailsAsync(params) { result, detailsList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK && detailsList.isNotEmpty()) {
                productDetails = detailsList.first()
                Log.d(TAG, "Produit trouvé: ${productDetails?.name}")
                // Remonte le prix formaté (localisé selon le pays du compte Play Store)
                productDetails?.oneTimePurchaseOfferDetails?.formattedPrice?.let { price ->
                    Log.d(TAG, "Prix récupéré: $price")
                    onPriceChanged?.invoke(price)
                }
            } else {
                Log.w(TAG, "Produit non trouvé: ${result.debugMessage}")
            }
        }
    }

    /**
     * Vérifie si l'utilisateur a déjà acheté "gainznote_remove_ads".
     * Public pour pouvoir être ré-appelée (ex: depuis onResume) afin de rattraper
     * un achat dont le callback onPurchasesUpdated n'aurait pas été reçu
     * (ex: Activity remise au premier plan après le flow d'achat Google Play).
     */
    fun queryExistingPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPurchase = purchases.any { purchase ->
                    purchase.products.contains(PRODUCT_ID) &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                if (hasPurchase != isAdFree) {
                    isAdFree = hasPurchase
                    onAdFreeChanged(isAdFree)
                    Log.d(TAG, "État adFree mis à jour: $isAdFree")
                }
                // Acknowledge les achats non confirmés
                purchases.filter {
                    it.products.contains(PRODUCT_ID) &&
                    it.purchaseState == Purchase.PurchaseState.PURCHASED &&
                    !it.isAcknowledged
                }.forEach { acknowledgePurchase(it) }
            }
        }
    }

    /**
     * Re-vérifie l'état d'achat auprès de Google Play.
     * À appeler depuis onResume() de l'Activity pour rattraper un achat dont le
     * callback onPurchasesUpdated aurait été manqué (ex: l'Activity est mise en
     * pause pendant l'écran de paiement Google Play, puis reprend la main).
     * Ne fait rien si le client Billing n'est pas encore prêt — il sera de toute
     * façon interrogé dès que startConnection() aura terminé.
     */
    fun refreshPurchases() {
        if (billingClient.isReady) {
            queryExistingPurchases()
        }
    }

    /** Lance le flow d'achat. */
    fun launchPurchase(activity: Activity): Boolean {
        val details = productDetails ?: run {
            Log.w(TAG, "ProductDetails pas encore chargé")
            return false
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build()
                )
            ).build()

        val result = billingClient.launchBillingFlow(activity, flowParams)
        return result.responseCode == BillingClient.BillingResponseCode.OK
    }

    /** Callback quand un achat est terminé ou annulé. */
    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.products.contains(PRODUCT_ID) &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                    ) {
                        isAdFree = true
                        onAdFreeChanged(true)
                        acknowledgePurchase(purchase)
                        onPurchaseJustCompleted?.invoke()
                        Log.d(TAG, "Achat réussi !")
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "Achat annulé par l'utilisateur")
            }
            else -> {
                Log.w(TAG, "Erreur achat: ${result.debugMessage}")
            }
        }
    }

    /** Confirme l'achat auprès de Google. */
    private fun acknowledgePurchase(purchase: Purchase) {
        if (purchase.isAcknowledged) return
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { result ->
            Log.d(TAG, "Acknowledge: ${result.responseCode}")
        }
    }

    /**
     * Restaure les achats existants depuis le Play Store.
     * Si le client est déconnecté, reconnecte d'abord puis interroge.
     * Appelle [onResult] avec true si un achat valide est trouvé, false sinon.
     */
    fun restorePurchases(onResult: (Boolean) -> Unit) {
        if (!billingClient.isReady) {
            Log.w(TAG, "restorePurchases: BillingClient non prêt, reconnexion...")
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Reconnexion réussie, lancement de la restauration")
                        queryProduct()
                        doQueryPurchases(onResult)
                    } else {
                        Log.w(TAG, "Reconnexion échouée: ${result.debugMessage}")
                        onResult(false)
                    }
                }
                override fun onBillingServiceDisconnected() {
                    Log.w(TAG, "Déconnecté pendant la restauration")
                    onResult(false)
                }
            })
            return
        }
        doQueryPurchases(onResult)
    }

    private fun doQueryPurchases(onResult: (Boolean) -> Unit) {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPurchase = purchases.any { purchase ->
                    purchase.products.contains(PRODUCT_ID) &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                if (hasPurchase) {
                    isAdFree = true
                    onAdFreeChanged(true)
                    purchases.filter {
                        it.products.contains(PRODUCT_ID) &&
                        it.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        !it.isAcknowledged
                    }.forEach { acknowledgePurchase(it) }
                }
                onResult(hasPurchase)
            } else {
                Log.w(TAG, "restorePurchases failed: ${result.debugMessage}")
                onResult(false)
            }
        }
    }

    fun destroy() {
        if (billingClient.isReady) billingClient.endConnection()
    }
}
