#!/bin/bash

# Script de validation du projet GainzNote
echo "🔍 Validation du projet GainzNote"
echo "================================="

# Couleurs pour l'affichage
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Fonction de validation
validate_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} $1"
        return 0
    else
        echo -e "${RED}❌${NC} $1 (manquant)"
        return 1
    fi
}

validate_dir() {
    if [ -d "$1" ]; then
        echo -e "${GREEN}✓${NC} $1/"
        return 0
    else
        echo -e "${RED}❌${NC} $1/ (manquant)"
        return 1
    fi
}

# Compteur d'erreurs
ERRORS=0

echo "📁 Structure des dossiers:"
validate_dir "androidApp" || ((ERRORS++))
validate_dir "composeApp" || ((ERRORS++))
validate_dir "gradle" || ((ERRORS++))

echo ""
echo "📄 Fichiers de configuration principaux:"
validate_file "build.gradle.kts" || ((ERRORS++))
validate_file "settings.gradle.kts" || ((ERRORS++))
validate_file "gradle.properties" || ((ERRORS++))
validate_file "androidApp/build.gradle.kts" || ((ERRORS++))
validate_file "composeApp/build.gradle.kts" || ((ERRORS++))

echo ""
echo "🔧 Scripts et configuration:"
validate_file "gradlew" || ((ERRORS++))
validate_file "fix_version_extraction.sh" || ((ERRORS++))
validate_file "codemagic.yaml" || ((ERRORS++))

echo ""
echo "📱 Configuration Android:"
validate_file "androidApp/src/androidMain/AndroidManifest.xml" || ((ERRORS++))

echo ""
echo "📚 Documentation:"
validate_file "README.md" || ((ERRORS++))
validate_file ".gitignore" || ((ERRORS++))

# Vérifications supplémentaires
echo ""
echo "🔍 Vérifications supplémentaires:"

# Vérifier que gradlew est exécutable
if [ -x "gradlew" ]; then
    echo -e "${GREEN}✓${NC} gradlew est exécutable"
else
    echo -e "${YELLOW}⚠️${NC} gradlew n'est pas exécutable (chmod +x gradlew)"
fi

# Vérifier le script fix
if [ -x "fix_version_extraction.sh" ]; then
    echo -e "${GREEN}✓${NC} fix_version_extraction.sh est exécutable"
else
    echo -e "${YELLOW}⚠️${NC} fix_version_extraction.sh n'est pas exécutable"
fi

# Vérifier local.properties
if [ -f "local.properties" ]; then
    echo -e "${GREEN}✓${NC} local.properties configuré"
else
    echo -e "${YELLOW}⚠️${NC} local.properties manquant (copiez local.properties.template)"
fi

echo ""
echo "📊 Résumé:"
if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}🎉 Projet validé avec succès ! Prêt pour le build.${NC}"
    echo ""
    echo "🚀 Commandes suggérées:"
    echo "  ./gradlew :androidApp:assembleDebug"
    echo "  ./fix_version_extraction.sh"
else
    echo -e "${RED}❌ $ERRORS erreur(s) détectée(s).${NC}"
    echo "Corrigez les fichiers manquants avant de continuer."
fi

exit $ERRORS
