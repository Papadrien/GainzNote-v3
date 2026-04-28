#!/bin/bash

# Script de fix pour l'extraction de version GainzNote
# Ce script corrige le problème de path pour versionName/versionCode

echo "🔧 GainzNote Build Fix Script"
echo "============================="

# Vérifier qu'on est dans la bonne directory
if [ ! -f "androidApp/build.gradle.kts" ]; then
    echo "❌ Erreur: androidApp/build.gradle.kts non trouvé"
    echo "   Assurez-vous d'être dans la racine du projet GainzNote"
    echo "   Ou ajustez le chemin vers le fichier"

    # Chercher le fichier dans les sous-dossiers
    echo "🔍 Recherche du fichier..."
    find . -name "build.gradle.kts" -path "*/androidApp/*" 2>/dev/null | head -5
    exit 1
fi

echo "✅ Fichier androidApp/build.gradle.kts trouvé"

# Extraire versionName (méthode robuste)
VERSION_NAME=$(grep -E '\s*versionName\s*=' androidApp/build.gradle.kts | head -n1 | sed -E 's/.*versionName\s*=\s*"([^"]+)".*/\1/')
VERSION_CODE=$(grep -E '\s*versionCode\s*=' androidApp/build.gradle.kts | head -n1 | sed -E 's/.*versionCode\s*=\s*([0-9]+).*/\1/')

if [ -z "$VERSION_NAME" ]; then
    echo "❌ Impossible d'extraire versionName"
    echo "📄 Contenu de la section defaultConfig:"
    grep -A 10 -B 2 "defaultConfig" androidApp/build.gradle.kts
    exit 1
fi

if [ -z "$VERSION_CODE" ]; then
    echo "❌ Impossible d'extraire versionCode"
    exit 1
fi

echo "✅ Version extraite avec succès:"
echo "   versionName: $VERSION_NAME"
echo "   versionCode: $VERSION_CODE"

# Exporter les variables pour utilisation dans CI/CD
export VERSION_NAME
export VERSION_CODE
echo "VERSION_NAME=$VERSION_NAME" >> $GITHUB_ENV
echo "VERSION_CODE=$VERSION_CODE" >> $GITHUB_ENV

echo "🎉 Fix appliqué avec succès!"
