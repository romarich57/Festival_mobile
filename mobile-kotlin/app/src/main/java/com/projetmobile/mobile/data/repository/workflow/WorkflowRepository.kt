package com.projetmobile.mobile.data.repository.workflow

import com.projetmobile.mobile.data.remote.reservation.WorkflowDto
import com.projetmobile.mobile.data.remote.reservation.WorkflowUpdatePayload

/**
 * Rôle : Définit le contrat d'accès et d'édition pour le Workflow (Ligne d'étapes d'une réservation).
 * C'est une sous-structure du processus de réservation qui documente les actions relationnelles.
 * 
 * Précondition : Injection de ce composant dans le DI.
 * Postcondition : Opère de façon synchrone avec l'API, directement connectée.
 */
interface WorkflowRepository {

    /**
     * Rôle : Soumission d'une nouvelle version de l'état relationnel (cases cochées / statuts logistiques modifiés).
     * 
     * Précondition : Payload contenant l'intégralité des booléens du workflow et l'ID Réservation.
     * Postcondition : Retourne la version distantement confirmée du Workflow via [WorkflowDto].
     */
    suspend fun updateWorkflow(reservationId:Int, data: WorkflowUpdatePayload): WorkflowDto

    /**
     * Rôle : Raparier l'état Workflow synchronisé d'une réservation.
     * 
     * Précondition : Un ID de réservation formel `reservationId`.
     * Postcondition : [WorkflowDto] structuré depuis JSON.
     */
    suspend fun getWorkflow(reservationId:Int): WorkflowDto

    /**
     * Rôle : Signaler qu'une tentative de contact / relance a été opérée aujourd'hui par l'admin.
     * 
     * Précondition : ID correct ciblé.
     * Postcondition : La date courante est estampillée dans la liste `contact_dates`.
     */
    suspend fun addContactDate(id:Int)
}