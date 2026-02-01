import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextDark
import com.example.focusme.presentation.ui.theme.TextGray
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksSheet(
    sessionSeconds: Int,
    tasks: List<String>,
    taskInput: String,
    onTaskInputChange: (String) -> Unit,
    onAddTask: () -> Unit,
    onRemoveTask: (Int) -> Unit,
    onPickFromPlanner: () -> Unit,
    onCancel: () -> Unit,
    onStart: () -> Unit,
    onClose: () -> Unit
) {
    val headerGrad = Brush.horizontalGradient(
        listOf(
            PinkPrimary,
            PinkPrimary.copy(alpha = 0.85f),
            Color(0xFFFFB3D1)
        )
    )

    // ✅ format temps (MM:SS)
    val timeText = remember(sessionSeconds) {
        val mm = (sessionSeconds.coerceAtLeast(0)) / 60
        val ss = (sessionSeconds.coerceAtLeast(0)) % 60
        "%02d:%02d".format(mm, ss)
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        dragHandle = null
    ) {
        // ✅ IMPORTANT: limite la hauteur pour qu’elle soit “plus haut” (comme ton exemple)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.86f) // ajuste: 0.82f / 0.86f / 0.90f
        ) {

            // HEADER (rose dégradé)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
                    .background(headerGrad)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Session d'étude",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = timeText,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f))
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(Modifier.height(14.dp))

            // ✅ le contenu au milieu peut scroller si besoin
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // prend l’espace restant entre header et boutons
                    .padding(horizontal = 18.dp)
            ) {
                // TITRE + compteur
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tâches à accomplir",
                        color = TextDark,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PinkPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tasks.size.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // INPUT + +
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = taskInput,
                        onValueChange = onTaskInputChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ex. Maths – chapitre 5") },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PinkPrimary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color(0xFFF3F3F3),
                            unfocusedContainerColor = Color(0xFFF3F3F3),
                            cursorColor = PinkPrimary
                        )
                    )

                    Spacer(Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFF3F3F3))
                            .clickable { onAddTask() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = TextDark)
                    }
                }

                Spacer(Modifier.height(10.dp))

                // ✅ LISTE tâches : prend l’espace dispo et scroll
                if (tasks.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 10.dp)
                    ) {
                        itemsIndexed(tasks) { index, t ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF7F7F7))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    t,
                                    color = TextDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "Supprimer",
                                    color = Color(0xFFD32F2F),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { onRemoveTask(index) }
                                )
                            }
                        }
                    }
                } else {
                    Spacer(Modifier.height(6.dp))
                }

                // "ou"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(modifier = Modifier.weight(1f), color = Color(0xFFEAEAEA))
                    Spacer(Modifier.width(10.dp))
                    Text("ou", color = TextGray, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(10.dp))
                    Divider(modifier = Modifier.weight(1f), color = Color(0xFFEAEAEA))
                }

                Spacer(Modifier.height(12.dp))

                // choisir depuis planner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFFBEAF2))
                        .clickable { onPickFromPlanner() }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = PinkPrimary)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Choisir des tâches depuis le\nplanificateur du jour",
                        color = PinkPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(14.dp))
            }

            // ✅ bottom buttons fixés
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(0.7f)   // ✅ plus petit
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = TextDark
                    )
                ) {
                    Text("Annuler", fontWeight = FontWeight.Bold, fontSize = 16.sp,)
                }

                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .weight(1.4f)   // ✅ plus grand
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
                ) {
                    Text(
                        text = "Démarrer ! 🚀",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color.White
                    )                }
            }

        }
    }
}
