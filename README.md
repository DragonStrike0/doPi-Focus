# 🎧 doPi focus

> **Digital Minimalism meets Retro Aesthetics.**

**doPi focus** est une application Android de productivité conçue pour lutter contre le "doomscrolling" et la fatigue numérique. En transformant votre smartphone en un baladeur numérique rétro (façon iPod Classic), l'application crée une "friction douce" (Soft Nudge) : elle vous isole des distractions tout en vous gardant dans un état de *Flow*.

![Status](https://img.shields.io/badge/Status-Work_in_Progress-orange)
![Platform](https://img.shields.io/badge/Platform-Android-green)
![Tech](https://img.shields.io/badge/UI-Jetpack_Compose-blue)

---

## ✨ Fonctionnalités Principales

* **🎛️ Click Wheel Mathématique & Haptique :** Une molette tactile recréée de zéro en utilisant la trigonométrie pour calculer la rotation du pouce, accompagnée d'un retour haptique (vibrations fines) à chaque cran.
* **📱 Interface "Liquid Glass" :** Un mélange de Skeuomorphisme (la roue) et de Glassmorphism (l'écran) pour un rendu néo-rétro élégant et reposant (Dark Mode natif pour écrans OLED).
* **🍅 Pomodoro & Do Not Disturb (DND) :** Lancez un timer de travail. L'application intercepte l'API système pour activer automatiquement le mode "Ne pas déranger", bloquant les notifications (Instagram, TikTok, etc.) pendant votre session.
* **🎵 Audio Intégré :** Lecteur audio natif (Brown Noise / Lo-Fi) intégré directement au timer pour masquer les bruits environnants.
* **🗂️ Navigation Drill-down :** Un système de menus en cascade fidèle aux lecteurs MP3 des années 2000.

---

## 📸 Captures d'écran

| Menu Principal | Mode Focus (Pomodoro) | Lecteur de Musique |
| :---: | :---: | :---: |
| `[Image 1]` | `[Image 2]` | `[Image 3]` |

---

## 🛠️ Stack Technique

Ce projet a été développé nativement pour Android en adoptant les standards modernes de Google :
* **Langage :** Kotlin
* **UI Framework :** Jetpack Compose (Material 3)
* **Asynchronisme :** Kotlin Coroutines (`LaunchedEffect`)
* **Audio :** `MediaPlayer` API
* **Système :** API `NotificationManager` (Focus Filters)

---

## 🚀 Installation & Test

1. Clonez ce dépôt : `git clone https://github.com/VOTRE_NOM/dopi-focus.git`
2. Ouvrez le projet dans **Android Studio**.
3. Lancez l'application sur un appareil physique (fortement recommandé). *Note : Les fonctionnalités haptiques et le mode DND ne réagissent pas bien sur l'émulateur.*
4. **Important :** Lors du premier lancement du timer, l'application vous demandera l'autorisation d'accéder au mode "Ne pas déranger" via les paramètres de votre téléphone.

---

## 🚧 Roadmap (À venir)
- [ ] Explorateur de fichiers MP3 locaux (MediaStore API).
- [ ] Lecture et affichage des tags ID3 (Pochettes d'album, Artiste, Paroles).
- [ ] Système de base de données (DataStore) pour la gamification (XP, Streaks).
- [ ] Déblocage de "Skins" (Couleurs d'iPod) en fonction du temps de concentration.

