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
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.media.MediaMetadataRetriever
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
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

    // États du Menu
    val currentScreen = remember { mutableStateOf("MENU") } // Peut être "MENU", "POMODORO", "MUSIC", "SETTINGS"
    val menuItems = listOf("Pomodoro Work", "Listen to Music", "Settings")
    val selectedMenuIndex = remember { mutableStateOf(0) }
    // NOUVEAU : Mémoire pour les chansons
    val songs = remember { mutableStateOf<List<Song>>(emptyList()) }
    val selectedSongIndex = remember { mutableStateOf(0) }
    val currentSong = remember { mutableStateOf<Song?>(null) }
    val currentCover = remember { mutableStateOf<ImageBitmap?>(null) }

    // NOUVEAU : Le "Videur" qui demande la permission
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Si on dit OUI, on lance la recherche et on change d'écran !
            songs.value = getLocalMusic(context)
            currentScreen.value = "SONG_LIST"
        }
    }

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
                    } else if (currentScreen.value == "SONG_LIST") {
                        // Si on valide une musique, on la charge et on passe à l'écran de lecture !
                        if (songs.value.isNotEmpty()) {
                            val songToPlay = songs.value[selectedSongIndex.value]
                            currentSong.value = songToPlay

                            // 1. Extraction de la pochette d'album
                            val retriever = MediaMetadataRetriever()
                            try {
                                retriever.setDataSource(context, songToPlay.uri)
                                val artBytes = retriever.embeddedPicture
                                if (artBytes != null) {
                                    // On transforme les données brutes en image affichable
                                    val bitmap = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size)
                                    currentCover.value = bitmap.asImageBitmap()
                                } else {
                                    currentCover.value = null // Pas de pochette trouvée
                                }
                            } catch (e: Exception) {
                                currentCover.value = null
                            } finally {
                                retriever.release()
                            }

                            // 2. Lancement de la musique
                            mediaPlayer.reset() // On vide le lecteur (enlève le bruit marron)
                            mediaPlayer.setDataSource(context, songToPlay.uri) // On met la nouvelle musique
                            mediaPlayer.prepare()
                            mediaPlayer.start()
                            isPlaying.value = true // On passe en mode Play

                            // 3. On change d'écran
                            currentScreen.value = "MUSIC"
                        }
                    }else if (currentScreen.value == "MUSIC") {
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
                            // Milieu : Pochette et Infos
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. La vraie pochette d'album
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(Color.DarkGray)
                                        .border(1.dp, Color.Gray)
                                ) {
                                    if (currentCover.value != null) {
                                        // Affiche l'image extraite du MP3
                                        Image(
                                            bitmap = currentCover.value!!,
                                            contentDescription = "Pochette d'album",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop // Recadre proprement en carré
                                        )
                                    } else {
                                        // Image par défaut s'il n'y a pas de pochette
                                        Text("🎵", modifier = Modifier.align(Alignment.Center), fontSize = 32.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // 2. Les informations dynamiques du morceau
                                Column {
                                    Text(
                                        text = currentSong.value?.title ?: "Sélectionnez un titre",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1 // Évite que le texte ne dépasse de l'écran
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentSong.value?.artist ?: "Artiste inconnu",
                                        color = Color.LightGray,
                                        fontSize = 14.sp,
                                        maxLines = 1
                                    )
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
                                    // SENS DES AIGUILLES
                                    if (currentScreen.value == "MENU") {
                                        if (selectedMenuIndex.value < menuItems.size - 1) selectedMenuIndex.value++
                                    } else if (currentScreen.value == "SONG_LIST") { // NOUVEAU
                                        if (selectedSongIndex.value < songs.value.size - 1) selectedSongIndex.value++
                                    } else if (currentScreen.value == "POMODORO" && !isPlaying.value) {
                                        timeRemaining.value += 30
                                    }
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    previousAngle = currentAngle

                                } else if (smoothDelta < -sensitivity) {
                                    // SENS INVERSE
                                    if (currentScreen.value == "MENU") {
                                        if (selectedMenuIndex.value > 0) selectedMenuIndex.value--
                                    } else if (currentScreen.value == "SONG_LIST") { // NOUVEAU
                                        if (selectedSongIndex.value > 0) selectedSongIndex.value--
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
                            if (selectedMenuIndex.value == 0) {
                                currentScreen.value = "POMODORO"
                            } else if (selectedMenuIndex.value == 1) {
                                // NOUVEAU : On demande la permission selon la version d'Android
                                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    Manifest.permission.READ_MEDIA_AUDIO // Android 13+
                                } else {
                                    Manifest.permission.READ_EXTERNAL_STORAGE // Android plus ancien
                                }
                                permissionLauncher.launch(permission)
                            }
                        } else if (currentScreen.value == "SONG_LIST") {
                            // Si on valide une musique, on passe à l'écran de lecture !
                            if (songs.value.isNotEmpty()) {
                                currentScreen.value = "MUSIC"
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
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val uri: Uri // L'adresse exacte du fichier dans le téléphone
)
fun getLocalMusic(context: Context): List<Song> {
    val songs = mutableListOf<Song>()

    // 1. On dit à Android où chercher (La mémoire externe)
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    }

    // 2. On choisit les colonnes qu'on veut lire dans la base de données
    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST
    )

    // 3. On filtre pour ne prendre QUE les musiques (pas les mémos vocaux par ex)
    val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

    // 4. On lance la recherche !
    context.contentResolver.query(
        collection,
        projection,
        selection,
        null,
        "${MediaStore.Audio.Media.TITLE} ASC" // Tri par ordre alphabétique
    )?.use { cursor ->
        // 5. On lit les résultats ligne par ligne
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val title = cursor.getString(titleColumn) ?: "Titre inconnu"
            val artist = cursor.getString(artistColumn) ?: "Artiste inconnu"
            val contentUri: Uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

            // On ajoute la chanson trouvée à notre liste
            songs.add(Song(id, title, artist, contentUri))
        }
    }
    return songs
}