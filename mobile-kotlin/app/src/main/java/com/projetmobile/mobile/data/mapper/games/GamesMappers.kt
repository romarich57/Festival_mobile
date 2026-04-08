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

/**
 * Rôle : Traduire la réponse paginée du réseau en modèle de pagination de domaine métier.
 * 
 * Précondition : `GamesPageResponseDto` recensant toutes les propriétés JSON renvoyées par le backend.
 * Postcondition : `PagedResult<GameListItem>` contenant la page courante, une vue liste de ses entités et les flags paginés (`hasNext`).
 */
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

/**
 * Rôle : Transformer unitairement l'entité DTO d'un jeu spécifique en élément léger de domaine (`GameListItem`).
 * 
 * Précondition : Un `GameDto` standard envoyé par l'API contenant un ensemble minimal de variables.
 * Postcondition : Map et retourne le `GameListItem` destiné à la liste des jeux.
 */
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

/**
 * Rôle : Assurer la conversion complète du DTO de jeu provenant de l'API REST en entité détaillée du domaine.
 * 
 * Précondition : Le DTO `GameDto` est complet et fourni avec la liste intégrale des objets `mechanisms`.
 * Postcondition : Transforme l'intégralité du DTO en un modèle riche (`GameDetail`) propre à l'écran de consultation d'un jeu.
 */
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

/**
 * Rôle : Traduire un DTO partiel de mécanisme serveur vers son entité optionnelle propre à la dropdown UI.
 * 
 * Précondition : Un `MechanismDto` valide retourné par le réseau.
 * Postcondition : `MechanismOption` prêt à l'emploi.
 */
fun MechanismDto.toMechanismOption(): MechanismOption = MechanismOption(
    id = id,
    name = name,
    description = description,
)

/**
 * Rôle : Traduire un DTO partiel d'éditeur serveur vers un objet entité local.
 * 
 * Précondition : Le DTO `EditorDto` est complet (inclut boolean exhibitor/distributor).
 * Postcondition : Map vers son équivalent modèle de sélection `EditorOption`.
 */
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

/**
 * Rôle : Extension pour la transformation simple d'une valeur de type primitif en Wrapper du domaine (`GameTypeOption`).
 * 
 * Précondition : Une chaîne de caractères non altérée.
 * Postcondition : Un conteneur objet `GameTypeOption` exposant cette seule chaîne en paramètre exploitable.
 */
fun String.toGameTypeOption(): GameTypeOption = GameTypeOption(
    value = this,
)
