package com.projetmobile.mobile.ui.screens.games

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameFormDraftMapperTest {

    private val mapper = DefaultGameFormDraftMapper()

    @Test
    fun toDraft_usesTrimmedImageUrlInUrlMode() {
        val draft = mapper.toDraft(
            fields = GameFormFields(
                title = "Akropolis",
                type = "Experts",
                editorId = 9,
                minAgeInput = "12",
                authors = "Designer",
                imageUrl = " https://cdn.example.com/game.png ",
                rulesVideoUrl = "   ",
            ),
            imageSourceMode = GameImageSourceMode.Url,
            uploadedImageUrl = null,
        )

        assertEquals("https://cdn.example.com/game.png", draft.imageUrl)
        assertNull(draft.rulesVideoUrl)
    }

    @Test
    fun toDraft_prefersUploadedImageInFileMode() {
        val draft = mapper.toDraft(
            fields = GameFormFields(
                title = "Akropolis",
                type = "Experts",
                editorId = 9,
                minAgeInput = "12",
                authors = "Designer",
                imageUrl = "https://old.example.com/game.png",
            ),
            imageSourceMode = GameImageSourceMode.File,
            uploadedImageUrl = "/uploads/games/new.png",
        )

        assertEquals("/uploads/games/new.png", draft.imageUrl)
    }
}
