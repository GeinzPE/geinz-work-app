package com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random

data class Item(val id: Int)





@Composable
fun HorizontalPinterestRow() {
    val lista = remember { List(20) { Item(it) } }
    val baseWidth = 120.dp
    val spacing = 16.dp

    LazyRow(
        contentPadding = PaddingValues(horizontal = spacing),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        modifier = Modifier.height(250.dp)
    ) {
        var index = 0
        while (index < lista.size) {
            val remaining = lista.size - index
            if (remaining >= 2 && index % 3 != 0) {
                // Bloque de 2 items juntos
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                        Box(
                            modifier = Modifier
                                .width(baseWidth)
                                .fillMaxHeight()
                                .background(randomColor(lista[index].id), shape = RoundedCornerShape(20.dp))
                        )
                        Box(
                            modifier = Modifier
                                .width(baseWidth)
                                .fillMaxHeight()
                                .background(randomColor(lista[index + 1].id), shape = RoundedCornerShape(20.dp))
                        )
                    }
                }
                index += 2
            } else {
                // Bloque de 1 item
                item {
                    Box(
                        modifier = Modifier
                            .width(baseWidth)
                            .fillMaxHeight()
                            .background(randomColor(lista[index].id), shape = RoundedCornerShape(20.dp) )
                    )
                }
                index += 1
            }
        }
    }
}

@Composable
fun randomColor(seed: Int): Color {
    return remember(seed) {
        Color(
            red = Random(seed).nextInt(50, 256) / 255f,
            green = Random(seed + 1).nextInt(50, 256) / 255f,
            blue = Random(seed + 2).nextInt(50, 256) / 255f,
            alpha = 1f
        )
    }
}

//@Composable
//fun galeriahorizontal(){
//    val numRows = 2
//    val rows = List(numRows) { mutableListOf<Item>() }
//
//    itemsList.forEachIndexed { index, item ->
//        rows[index % numRows].add(item)
//    }
//
//    LazyRow(
//        horizontalArrangement = Arrangement.spacedBy(16.dp),
//        contentPadding = PaddingValues(horizontal = 16.dp)
//    ) {
//        items(rows[0].size) { colIndex ->
//            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
//                rows.forEach { row ->
//                    row.getOrNull(colIndex)?.let { item ->
//                        Box(
//                            modifier = Modifier
//                                .width(120.dp)
//                                .height(if ((0..1).random() == 0) 150.dp else 200.dp)
//                                .background(Color.Red)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}