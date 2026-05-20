package fr.junade.gainznote.ui.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
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

        SelectionContainer {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                PrivacySection(
                    title = "Introduction",
                    body = if (S.lang == Lang.FR)
                        "GainzNote (l'« Application ») est développée et maintenue par Junadé.\nCette politique de confidentialité explique quelles données peuvent être utilisées lorsque vous utilisez l'Application."
                    else
                        "GainzNote (the \"App\") is developed and maintained by Junadé.\nThis privacy policy explains what data may be used when you use the App.",
                    c = c
                )

                PrivacySection(
                    title = if (S.lang == Lang.FR) "Données d'entraînement" else "Workout Data",
                    body = if (S.lang == Lang.FR)
                        "Toutes vos données d'entraînement (séances, exercices, notes et statistiques) sont stockées localement sur votre appareil.\nL'Application ne transmet pas ces données à des serveurs externes."
                    else
                        "All your workout data (sessions, exercises, notes and statistics) is stored locally on your device.\nThe App does not transmit this data to external servers.",
                    c = c
                )

                PrivacySection(
                    title = if (S.lang == Lang.FR) "Publicités" else "Advertising",
                    body = if (S.lang == Lang.FR)
                        "L'Application utilise les services Google AdMob afin d'afficher des publicités.\nGoogle peut collecter certaines données techniques et identifiants publicitaires conformément à sa propre politique de confidentialité, notamment :\n\n• identifiant publicitaire ;\n• informations sur l'appareil ;\n• interactions avec les publicités.\n\nPour en savoir plus, consultez la politique de confidentialité de Google.\n\nLes utilisateurs peuvent supprimer les publicités via un achat intégré."
                    else
                        "The App uses Google AdMob services to display advertisements.\nGoogle may collect certain technical data and advertising identifiers in accordance with its own privacy policy, including:\n\n• advertising identifier;\n• device information;\n• ad interactions.\n\nFor more information, please refer to Google's privacy policy.\n\nUsers can remove ads via an in-app purchase.",
                    c = c
                )

                PrivacySection(
                    title = if (S.lang == Lang.FR) "Achats intégrés" else "In-App Purchases",
                    body = if (S.lang == Lang.FR)
                        "Les achats intégrés sont gérés par Google Play Billing sur Android et par Apple In-App Purchases sur iOS.\nAucune information bancaire ou de paiement n'est traitée ni stockée directement par l'Application."
                    else
                        "In-app purchases are handled by Google Play Billing on Android and Apple In-App Purchases on iOS.\nNo banking or payment information is processed or stored directly by the App.",
                    c = c
                )

                PrivacySection(
                    title = if (S.lang == Lang.FR) "Notifications" else "Notifications",
                    body = if (S.lang == Lang.FR)
                        "L'Application peut demander l'autorisation d'envoyer des notifications, notamment pour le chronomètre de repos.\nCette autorisation est facultative et peut être désactivée à tout moment dans les paramètres de votre appareil."
                    else
                        "The App may request permission to send notifications, including for the rest timer.\nThis permission is optional and can be disabled at any time in your device settings.",
                    c = c
                )

                PrivacySection(
                    title = if (S.lang == Lang.FR) "Export des données" else "Data Export",
                    body = if (S.lang == Lang.FR)
                        "L'Application permet d'exporter vos données d'entraînement dans un fichier JSON local afin de faciliter vos sauvegardes personnelles."
                    else
                        "The App allows you to export your workout data to a local JSON file to facilitate personal backups.",
                    c = c
                )



                PrivacySection(
                    title = if (S.lang == Lang.FR) "Conservation et suppression des données" else "Data Retention and Deletion",
                    body = if (S.lang == Lang.FR)
                        """Les données d'entraînement de l'utilisateur sont stockées uniquement localement sur son appareil et ne sont pas transmises à des serveurs externes.

Ces données sont conservées jusqu'à ce que l'utilisateur :

• les supprime manuellement depuis l'application ;
• efface les données de l'application depuis les paramètres de son appareil ;
• ou désinstalle l'application.

Lors de la désinstallation de l'application, toutes les données locales associées peuvent être supprimées automatiquement par le système d'exploitation.

Certaines données publicitaires peuvent être collectées et conservées par Google AdMob conformément à la politique de confidentialité de Google.

Le développeur n'a pas accès aux données d'entraînement des utilisateurs et ne peut pas les supprimer à distance."""
                    else
                        """User workout data is stored locally on the user's device only and is not transmitted to external servers.

This data is retained until the user:

• manually deletes it from the application;
• clears the app data from the device settings;
• or uninstalls the application.

When the application is uninstalled, all associated local data may be automatically removed by the operating system.

Some advertising-related data may be collected and retained by Google AdMob in accordance with Google's privacy policy.

The developer does not have access to user workout data and cannot delete it remotely.""",
                    c = c
                )

                PrivacySection(
                    title = if (S.lang == Lang.FR) "Sécurité" else "Security",
                    body = if (S.lang == Lang.FR)
                        "Les données restent stockées localement sur votre appareil.\nVous êtes responsable de la sécurité et des sauvegardes de votre appareil et des fichiers exportés."
                    else
                        "Data remains stored locally on your device.\nYou are responsible for the security and backups of your device and exported files.",
                    c = c
                )

                PrivacySection(
                    title = if (S.lang == Lang.FR) "Modifications" else "Changes",
                    body = if (S.lang == Lang.FR)
                        "Cette politique de confidentialité peut être mise à jour afin de refléter les évolutions de l'Application ou des obligations légales."
                    else
                        "This privacy policy may be updated to reflect changes to the App or legal requirements.",
                    c = c
                )

                PrivacySection(
                    title = "Contact",
                    body = if (S.lang == Lang.FR)
                        "Pour toute question concernant cette politique de confidentialité, vous pouvez contacter le développeur à l'adresse suivante :\npapadrien.prepa@gmail.com"
                    else
                        "For any questions regarding this privacy policy, you can contact the developer at:\npapadrien.prepa@gmail.com",
                    c = c
                )

                Spacer(Modifier.height(32.dp))
            }
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
