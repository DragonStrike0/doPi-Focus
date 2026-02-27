package com.DragonStrike.ipodfocus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.clickable
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
import android.media.MediaPlayer
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import kotlin.math.atan2
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.width

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    IpodScreen()
                }
            }
        }
    }
}



@Composable
fun IpodScreen() {
    // --- 1. LES VARIABLES D'ÉTAT ---
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.focus) }

    // États du Pomodoro
    val isPlaying = remember { mutableStateOf(false) }
    val timeRemaining = remember { mutableStateOf(45 * 60) }

    // NOUVEAU : États du Menu
    val currentScreen = remember { mutableStateOf("MENU") } // Peut être "MENU", "POMODORO", "MUSIC", "SETTINGS"
    val menuItems = listOf("Pomodoro Work", "Listen to Music", "Settings")
    val selectedMenuIndex = remember { mutableStateOf(0) }

    // --- 2. LE MOTEUR DU TEMPS ---
    LaunchedEffect(isPlaying.value) {
        if (isPlaying.value) {
            while (timeRemaining.value > 0 && isPlaying.value) {
                delay(1000L)
                timeRemaining.value -= 1
            }
            if (timeRemaining.value == 0) {
                isPlaying.value = false
                mediaPlayer.pause()
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
        }
    }

    val minutes = timeRemaining.value / 60
    val seconds = timeRemaining.value % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    // --- 3. L'INTERFACE GRAPHIQUE ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top = 32.dp) // Laisse de la place en haut à cause du plein écran
    ) {
        // === MOITIÉ HAUT : L'ÉCRAN LIQUID GLASS ===
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(32.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // LA BARRE SUPÉRIEURE (doPi focus)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.1f)) // Légère barre grisée
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("12:00", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) // Heure (statique pour l'instant)
                    Text("doPi focus", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text("100% 🔋", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)                }

                // LE CONTENU DE L'ÉCRAN (Change selon currentScreen)
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {

                    if (currentScreen.value == "MENU") {
                        // DESSIN DU MENU
                        Column(modifier = Modifier.fillMaxWidth()) {
                            menuItems.forEachIndexed { index, item ->
                                val isSelected = index == selectedMenuIndex.value
                                Text(
                                    text = if (isSelected) "> $item" else "  $item",
                                    color = if (isSelected) Color.White else Color.Gray,
                                    fontSize = 20.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent)
                                        .padding(vertical = 8.dp, horizontal = 8.dp)
                                )
                            }
                        }
                    } else if (currentScreen.value == "POMODORO") {
                        // DESSIN DU POMODORO
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val statusText = when {
                                isPlaying.value -> "EN COURS 🔴"
                                timeRemaining.value == 45 * 60 -> "PRÊT À DÉMARRER" // Si le chrono est plein
                                else -> "EN PAUSE ⏸️" // Si on a mis pause en cours de route
                            }

                            Text(
                                text = statusText,
                                color = Color.Gray,
                                letterSpacing = 2.sp,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = timeString, color = Color.White, fontSize = 56.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Brown Noise - focus.mp3", color = Color.LightGray, fontSize = 16.sp)
                        }
                    } else if (currentScreen.value == "MUSIC") {
                        // --- DESSIN DE L'ÉCRAN LECTEUR MUSIQUE ---
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Haut : Indicateur "Now Playing"
                            Text(
                                text = "En lecture",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            // Milieu : Pochette et Infos
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. La fausse pochette d'album (Carré gris pour le moment)
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(Color.DarkGray)
                                        .border(1.dp, Color.Gray)
                                ) {
                                    // On y mettra la vraie cover plus tard
                                    Text("🎵", modifier = Modifier.align(Alignment.Center), fontSize = 32.sp)
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // 2. Les informations du morceau
                                Column {
                                    Text(text = "Titre du morceau", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Nom de l'Artiste", color = Color.LightGray, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Album", color = Color.Gray, fontSize = 12.sp)
                                }
                            }

                            // Bas : Barre de progression (Timeline)
                            Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .background(Color.DarkGray)
                                ) {
                                    // La partie "avancée" de la barre (ex: à 30%)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.3f)
                                            .fillMaxHeight()
                                            .background(Color.White)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("1:05", color = Color.White, fontSize = 10.sp) // Temps actuel
                                    Text("-2:25", color = Color.White, fontSize = 10.sp) // Temps restant
                                }
                            }
                        }
                    }
                }
            }
        }

        // === MOITIÉ BAS : LA CLICK WHEEL ===
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center
        ) {
            // 1. La Roue (Zone Tactile)
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .pointerInput(Unit) {
                        var previousAngle = 0.0
                        detectDragGestures(
                            onDragStart = { offset ->
                                val x = offset.x - (size.width / 2)
                                val y = offset.y - (size.height / 2)
                                previousAngle = Math.toDegrees(atan2(y.toDouble(), x.toDouble()))
                            },
                            onDrag = { change, _ ->
                                val x = change.position.x - (size.width / 2)
                                val y = change.position.y - (size.height / 2)
                                val currentAngle = Math.toDegrees(atan2(y.toDouble(), x.toDouble()))
                                val delta = currentAngle - previousAngle
                                val smoothDelta = when {
                                    delta > 180 -> delta - 360
                                    delta < -180 -> delta + 360
                                    else -> delta
                                }

                                val sensitivity = if (currentScreen.value == "MENU") 40 else 15

                                if (smoothDelta > sensitivity) {
                                    // SENS DES AIGUILLES
                                    if (currentScreen.value == "MENU") {
                                        if (selectedMenuIndex.value < menuItems.size - 1) selectedMenuIndex.value++
                                    } else if (currentScreen.value == "POMODORO" && !isPlaying.value) {
                                        timeRemaining.value += 30
                                    }
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    previousAngle = currentAngle

                                } else if (smoothDelta < -sensitivity) {
                                    // SENS INVERSE
                                    if (currentScreen.value == "MENU") {
                                        if (selectedMenuIndex.value > 0) selectedMenuIndex.value--
                                    } else if (currentScreen.value == "POMODORO" && !isPlaying.value && timeRemaining.value > 60) {
                                        timeRemaining.value -= 30
                                    }
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    previousAngle = currentAngle
                                }
                            }
                        )
                    }
            ) {
                // LES TEXTES SUR LA ROUE
                Text("MENU", color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp)
                    .clickable {
                        // 1. On retourne au menu
                        currentScreen.value = "MENU"

                        // 2. NOUVEAU : On force la pause si le timer tournait
                        if (isPlaying.value) {
                            isPlaying.value = false
                            mediaPlayer.pause()
                            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                        }
                    }
                )
                Text("⏭", color = Color.Gray, fontSize = 24.sp, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp))
                Text("⏮", color = Color.Gray, fontSize = 24.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp))
                Text("⏯", color = Color.Gray, fontSize = 24.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp))
            }

            // 2. Le Bouton Central (OK / Play-Pause)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
                    .clickable {
                        if (currentScreen.value == "MENU") {
                            // Action du bouton central dans le menu : "OK"
                            if (selectedMenuIndex.value == 0) {
                                currentScreen.value = "POMODORO"
                            }
                            else if (selectedMenuIndex.value == 1) {
                                currentScreen.value = "MUSIC" //
                            }
                        } else if (currentScreen.value == "POMODORO") {
                            // Action du bouton central dans le Pomodoro : "Play/Pause"
                            if (notificationManager.isNotificationPolicyAccessGranted) {
                                isPlaying.value = !isPlaying.value
                                if (isPlaying.value) {
                                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                                    mediaPlayer.start()
                                } else {
                                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                                    mediaPlayer.pause()
                                }
                            } else {
                                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                context.startActivity(intent)
                            }
                        }
                    }
            )
        }
    }
}