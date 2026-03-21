package com.projetmobile.mobile.data.mapper.games

import com.projetmobile.mobile.data.entity.games.EditorOption
import com.projetmobile.mobile.data.entity.games.GameDetail
import com.projetmobile.mobile.data.entity.games.GameListItem
import com.projetmobile.mobile.data.entity.games.GameTypeOption
import com.projetmobile.mobile.data.entity.games.MechanismOption
import com.projetmobile.mobile.data.entity.games.PagedResult
import com.projetmobile.mobile.data.remote.games.EditorDto
import com.projetmobile.mobile.data.remote.games.GameDto
import com.projetmobile.mobile.data.remote.games.GamesPageResponseDto
import com.projetmobile.mobile.data.remote.games.MechanismDto

fun GamesPageResponseDto.toGameListPage(): PagedResult<GameListItem> {
    val pageInfo = pagination
    return PagedResult(
        items = items.map(GameDto::toGameListItem),
        page = pageInfo.page,
        limit = pageInfo.limit,
        total = pageInfo.total,
        hasNext = pageInfo.page < pageInfo.totalPages,
    )
}

fun GameDto.toGameListItem(): GameListItem = GameListItem(
    id = id,
    title = title,
    type = type,
    editorId = editorId,
    editorName = editorName,
    minAge = minAge,
    authors = authors,
    minPlayers = minPlayers,
    maxPlayers = maxPlayers,
    prototype = prototype,
    durationMinutes = durationMinutes,
    theme = theme,
    description = description,
    imageUrl = imageUrl,
    rulesVideoUrl = rulesVideoUrl,
    mechanisms = mechanisms.map(MechanismDto::toMechanismOption),
)

fun GameDto.toGameDetail(): GameDetail = GameDetail(
    id = id,
    title = title,
    type = type,
    editorId = editorId,
    editorName = editorName,
    minAge = minAge,
    authors = authors,
    minPlayers = minPlayers,
    maxPlayers = maxPlayers,
    prototype = prototype,
    durationMinutes = durationMinutes,
    theme = theme,
    description = description,
    imageUrl = imageUrl,
    rulesVideoUrl = rulesVideoUrl,
    mechanisms = mechanisms.map(MechanismDto::toMechanismOption),
)

fun MechanismDto.toMechanismOption(): MechanismOption = MechanismOption(
    id = id,
    name = name,
    description = description,
)

fun EditorDto.toEditorOption(): EditorOption = EditorOption(
    id = id,
    name = name,
    email = email,
    website = website,
    description = description,
    logoUrl = logoUrl,
    isExhibitor = isExhibitor,
    isDistributor = isDistributor,
)

fun String.toGameTypeOption(): GameTypeOption = GameTypeOption(
    value = this,
)
