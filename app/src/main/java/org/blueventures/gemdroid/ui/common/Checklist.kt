package org.blueventures.gemdroid.ui.common

import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R

object Checklist {
    @Composable
    fun Screen(snack: SnackFun, title: String, items: List<String>, initState: Boolean, min: Int, next: (List<String>) -> Unit) {
        val pairs = mutableListOf<Pair<String, Boolean>>()

        for (item in items) {
            pairs.add(Pair(item, initState))
        }

        val checkMap = remember { mutableStateMapOf(*pairs.toTypedArray()) }

        Col.MidPad(scroll = true) {
            Info.Header(title)
            Info.Block {
                for (item in items) {
                    Info.Row {
                        Info.Txt("$item:")
                        Checkbox(checkMap[item]!!, onCheckedChange = { check ->
                            checkMap[item] = check
                        })
                    }
                    Info.BlueLine()
                }
            }
            val minMsg = stringResource(R.string.please_select_at_least).format(min.toString())
            Butt.Next {
                val checked = mutableListOf<String>()
                for (item in items) {
                    if (checkMap[item] == true) {
                        checked.add(item)
                    }
                }

                if (checked.size < min) {
                    snack(minMsg)
                } else {
                    next(checked)
                }
            }
        }
    }
}