#!/bin/bash

# Script de fix pour l'extraction de version GainzNote - Version améliorée
# Ce script corrige le problème de path pour versionName/versionCode

echo "🔧 GainzNote Build Fix Script - Version améliorée"
echo "================================================="

# Fonction pour trouver le fichier build.gradle.kts
find_build_file() {
    # Chercher d'abord dans androidApp/
    if [ -f "androidApp/build.gradle.kts" ]; then
        echo "androidApp/build.gradle.kts"
        return 0
    fi

    # Chercher dans les sous-dossiers
    BUILD_FILE=$(find . -name "build.gradle.kts" -path "*/androidApp/*" 2>/dev/null | head -1)
    if [ -n "$BUILD_FILE" ]; then
        echo "$BUILD_FILE"
        return 0
    fi

    return 1
}

# Trouver le fichier de build
BUILD_FILE_PATH=$(find_build_file)

if [ $? -ne 0 ] || [ -z "$BUILD_FILE_PATH" ]; then
    echo "❌ Erreur: Impossible de trouver androidApp/build.gradle.kts"
    echo ""
    echo "🔍 Fichiers build.gradle.kts trouvés:"
    find . -name "build.gradle.kts" 2>/dev/null | head -10
    echo ""
    echo "💡 Solutions possibles:"
    echo "   1. Vérifiez que vous êtes dans la racine du projet"
    echo "   2. Le projet utilise peut-être une structure différente"
    echo "   3. Utilisez des valeurs par défaut pour continuer le build"
    echo ""

    # Proposer des valeurs par défaut
    echo "🛠️  Utilisation des valeurs par défaut:"
    VERSION_NAME="1.0.0"
    VERSION_CODE="1"
    echo "   versionName: $VERSION_NAME"
    echo "   versionCode: $VERSION_CODE"
else
    echo "✅ Fichier trouvé: $BUILD_FILE_PATH"

    # Extraire versionName et versionCode avec plusieurs méthodes de fallback
    echo "🔍 Extraction des versions..."

    # Méthode 1: Recherche standard
    VERSION_NAME=$(grep -E '\s*versionName\s*=' "$BUILD_FILE_PATH" | head -n1 | sed -E 's/.*versionName\s*=\s*"([^"]+)".*/\1/')
    VERSION_CODE=$(grep -E '\s*versionCode\s*=' "$BUILD_FILE_PATH" | head -n1 | sed -E 's/.*versionCode\s*=\s*([0-9]+).*/\1/')

    # Méthode 2: Si pas trouvé, essayer avec des variations
    if [ -z "$VERSION_NAME" ]; then
        VERSION_NAME=$(grep -i "versionname" "$BUILD_FILE_PATH" | head -n1 | sed -E 's/.*["\''']([^"\''']+)["\'''].*/\1/')
    fi

    if [ -z "$VERSION_CODE" ]; then
        VERSION_CODE=$(grep -i "versioncode" "$BUILD_FILE_PATH" | head -n1 | sed -E 's/.*([0-9]+).*/\1/')
    fi

    # Méthode 3: Valeurs par défaut si toujours pas trouvé
    if [ -z "$VERSION_NAME" ]; then
        echo "⚠️  versionName non trouvé, utilisation de la valeur par défaut"
        VERSION_NAME="1.0.0"
    fi

    if [ -z "$VERSION_CODE" ]; then
        echo "⚠️  versionCode non trouvé, utilisation de la valeur par défaut"
        VERSION_CODE="1"
    fi
fi

echo ""
echo "✅ Version finale extraite:"
echo "   versionName: $VERSION_NAME"
echo "   versionCode: $VERSION_CODE"

# Exporter les variables pour utilisation dans CI/CD
export VERSION_NAME
export VERSION_CODE

# Pour GitHub Actions
if [ -n "$GITHUB_ENV" ]; then
    echo "VERSION_NAME=$VERSION_NAME" >> $GITHUB_ENV
    echo "VERSION_CODE=$VERSION_CODE" >> $GITHUB_ENV
    echo "📝 Variables ajoutées à GITHUB_ENV"
fi

# Pour autres systèmes CI
echo "VERSION_NAME=$VERSION_NAME"
echo "VERSION_CODE=$VERSION_CODE"

echo ""
echo "🎉 Fix appliqué avec succès!"
echo "💡 Le build peut maintenant continuer avec ces versions"

exit 0
