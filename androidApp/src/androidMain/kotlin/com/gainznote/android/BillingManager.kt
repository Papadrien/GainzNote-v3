package com.gainznote.android

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.*

/**
 * Gère l'achat in-app "remove_ads" via Google Play Billing.
 *
 * Utilisation :
 *  - Appeler [startConnection] au démarrage (onCreate).
 *  - Appeler [launchPurchase] pour déclencher le flow d'achat.
 *  - Lire [isAdFree] pour savoir si l'utilisateur a acheté.
 *  - [onAdFreeChanged] est appelé quand l'état change (pour mettre à jour l'UI).
 */
class BillingManager(
    context: Context,
    private val onAdFreeChanged: (Boolean) -> Unit
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "GainzBilling"
        const val PRODUCT_ID = "remove_ads"
    }

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private var productDetails: ProductDetails? = null
    var isAdFree: Boolean = false
        private set

    /** Connecte le BillingClient et vérifie les achats existants. */
    fun startConnection() {
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
    }

    /** Récupère les détails du produit "remove_ads". */
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
            } else {
                Log.w(TAG, "Produit non trouvé: ${result.debugMessage}")
            }
        }
    }

    /** Vérifie si l'utilisateur a déjà acheté "remove_ads". */
    private fun queryExistingPurchases() {
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

    fun destroy() {
        if (billingClient.isReady) billingClient.endConnection()
    }
}
