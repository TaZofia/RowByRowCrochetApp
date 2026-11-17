package com.example.rowbyrowapp.ui.editor.model

import androidx.annotation.DrawableRes
import com.example.rowbyrowapp.R
import com.example.rowbyrowapp.domain.model.StitchType

data class StitchItem (
    val id: Int,
    val name: String,
    val description: String,
    @DrawableRes val iconRes: Int,
    val type: StitchType? = null,
    val isSlipStitch: Boolean = false
)

val availableStitches = listOf(
    StitchItem(1, "CH", "chain", R.drawable.ch, type = StitchType.CH),
    StitchItem(2, "SL-ST", "slip-stitch",R.drawable.slst, isSlipStitch = true),
    StitchItem(3, "SC", "single crochet", R.drawable.sc, type = StitchType.SC),
    StitchItem(4, "HDC", "half-double crochet", R.drawable.hdc, type = StitchType.HDC),
    StitchItem(5, "DC", "double crochet", R.drawable.dc, type = StitchType.DC),
    StitchItem(6, "TR", "treble crochet", R.drawable.tr, type = StitchType.TR),
    StitchItem(7, "DTR", "double-treble crochet", R.drawable.dtr, type = StitchType.DTR)
)