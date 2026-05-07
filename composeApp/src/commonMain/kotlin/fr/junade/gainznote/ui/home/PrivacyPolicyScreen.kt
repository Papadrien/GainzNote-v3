package fr.junade.gainznote.ui.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.junade.gainznote.i18n.Lang
import fr.junade.gainznote.i18n.S
import fr.junade.gainznote.ui.theme.GainzThemeColors

@Composable
fun PrivacyPolicyScreen(
    darkTheme: Boolean,
    onBack: () -> Unit
) {
    val c = GainzThemeColors(darkTheme)

    Column(
        Modifier
            .fillMaxSize()
            .background(c.background)
            .safeDrawingPadding()
    ) {
        // TopBar
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(40.dp).clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Text("←", color = c.accent, fontSize = 22.sp)
            }
            Spacer(Modifier.width(8.dp))
            Text(S.privacyPolicy, color = c.accent, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
        HorizontalDivider(color = c.border, thickness = 0.5.dp)

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            PrivacySection(
                title = if (S.lang == Lang.FR) "Introduction" else "Introduction",
                body = if (S.lang == Lang.FR)
                    "GainzNote (\"l'Application\") est développée et maintenue par Junadé. Cette politique de confidentialité décrit comment l'Application collecte, utilise et protège vos données."
                else
                    "GainzNote (\"the App\") is developed and maintained by Junadé. This privacy policy describes how the App collects, uses, and protects your data.",
                c = c
            )

            PrivacySection(
                title = if (S.lang == Lang.FR) "Données collectées" else "Data We Collect",
                body = if (S.lang == Lang.FR)
                    "L'Application ne collecte aucune donnée personnelle identifiable. Toutes vos données d'entraînement sont stockées localement sur votre appareil et ne sont jamais transmises à des serveurs externes."
                else
                    "The App does not collect any personally identifiable information. All your workout data is stored locally on your device and is never transmitted to external servers.",
                c = c
            )

            PrivacySection(
                title = if (S.lang == Lang.FR) "Publicités" else "Advertising",
                body = if (S.lang == Lang.FR)
                    "L'Application utilise Google AdMob pour afficher des publicités. Google AdMob peut collecter certaines données (identifiants publicitaires, interactions) conformément à sa propre politique de confidentialité. Vous pouvez supprimer les publicités via un achat intégré."
                else
                    "The App uses Google AdMob to display advertisements. Google AdMob may collect certain data (advertising identifiers, interactions) in accordance with its own privacy policy. You can remove ads via an in-app purchase.",
                c = c
            )

            PrivacySection(
                title = if (S.lang == Lang.FR) "Achats intégrés" else "In-App Purchases",
                body = if (S.lang == Lang.FR)
                    "Les achats intégrés sont gérés par Google Play Billing. Aucune information de paiement n'est traitée directement par l'Application."
                else
                    "In-app purchases are handled by Google Play Billing. No payment information is processed directly by the App.",
                c = c
            )

            PrivacySection(
                title = if (S.lang == Lang.FR) "Permissions" else "Permissions",
                body = if (S.lang == Lang.FR)
                    "L'Application peut demander la permission d'envoyer des notifications (pour le chronomètre de repos). Cette permission est optionnelle et peut être révoquée dans les paramètres de votre appareil."
                else
                    "The App may request permission to send notifications (for the rest timer). This permission is optional and can be revoked in your device settings.",
                c = c
            )

            PrivacySection(
                title = if (S.lang == Lang.FR) "Sécurité des données" else "Data Security",
                body = if (S.lang == Lang.FR)
                    "Vos données d'entraînement restent sur votre appareil. La fonction d'export vous permet de sauvegarder vos données dans un fichier JSON local."
                else
                    "Your workout data remains on your device. The export feature allows you to back up your data to a local JSON file.",
                c = c
            )

            PrivacySection(
                title = if (S.lang == Lang.FR) "Contact" else "Contact",
                body = if (S.lang == Lang.FR)
                    "Pour toute question concernant cette politique de confidentialité, vous pouvez nous contacter via le site web : junade.vercel.app"
                else
                    "For any questions regarding this privacy policy, you can contact us via the website: junade.vercel.app",
                c = c
            )

            PrivacySection(
                title = if (S.lang == Lang.FR) "Modifications" else "Changes",
                body = if (S.lang == Lang.FR)
                    "Cette politique de confidentialité peut être mise à jour occasionnellement. Les modifications seront publiées sur notre site web."
                else
                    "This privacy policy may be updated occasionally. Changes will be posted on our website.",
                c = c
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PrivacySection(title: String, body: String, c: GainzThemeColors) {
    Spacer(Modifier.height(16.dp))
    Text(title, color = c.accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(6.dp))
    Text(body, color = c.textSec, fontSize = 14.sp, lineHeight = 20.sp)
}


