package com.example.focusme.presentation.screen.music

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.focusme.presentation.ui.theme.*

@Composable
fun SubscriptionManagementScreen(
    onBack: () -> Unit,
    vm: MusicSubscriptionViewModel = viewModel()
) {
    val ui by vm.ui.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
    ) {
        // Header
        Row(
            modifier = Modifier
                .padding(24.dp)
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    "Music Programming",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
                Text(
                    "Configure your study soundscape",
                    fontSize = 14.sp,
                    color = TextGray
                )
            }
        }

        if (ui.loading && ui.packs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PinkPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "Available Packs",
                        fontWeight = FontWeight.Bold,
                        color = PinkPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(ui.packs) { pack ->
                    // Détection simplifiée de l'abonnement (basée sur une logique locale ou backend)
                    // Pour la démo, on simule l'état
                    var isSubscribed by remember { mutableStateOf(false) } 

                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = pack.imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                            
                            Spacer(Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    pack.name,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = TextDark
                                )
                                Text(
                                    pack.description,
                                    fontSize = 12.sp,
                                    color = TextGray,
                                    maxLines = 2
                                )
                            }
                            
                            Switch(
                                checked = isSubscribed,
                                onCheckedChange = { 
                                    isSubscribed = it
                                    vm.toggleSubscription(pack.id, !it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PinkPrimary,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = BorderSoft
                                )
                            )
                        }
                    }
                }
                
                item {
                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }
}
