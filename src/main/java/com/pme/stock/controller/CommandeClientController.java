package com.pme.stock.controller;

import com.pme.stock.dto.request.CommandeClientRequest;
import com.pme.stock.dto.request.LigneCommandeRequest;
import com.pme.stock.dto.response.CommandeClientResponse;
import com.pme.stock.dto.response.PageResponse;
import com.pme.stock.entity.StatutCommande;
import com.pme.stock.service.CommandeClientService;
import com.pme.stock.util.PageableUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/commandes")
@RequiredArgsConstructor
@Tag(name = "Commande Client", description = "API de gestion des commandes clients")
@SecurityRequirement(name = "bearerAuth")
public class CommandeClientController {

    private static final String DEFAULT_COMMANDE_SORT = "dateCommande";

    private final CommandeClientService commandeClientService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Créer une nouvelle commande")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Commande créée",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommandeClientResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Client ou produit introuvable",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<CommandeClientResponse> creer(@Valid @RequestBody CommandeClientRequest request) {
        CommandeClientResponse commande = commandeClientService.creer(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(commande.getId()).toUri();
        return ResponseEntity.created(location).body(commande);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Récupérer une commande par son ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Commande trouvée",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommandeClientResponse.class))),
            @ApiResponse(responseCode = "404", description = "Commande introuvable",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<CommandeClientResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(commandeClientService.trouverParId(id));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister toutes les commandes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste paginée des commandes",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "Non authentifié",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<PageResponse<CommandeClientResponse>> listerToutes(
            @PageableDefault(size = 20, sort = DEFAULT_COMMANDE_SORT, direction = Sort.Direction.DESC) Pageable pageable) {
        Pageable safePageable = PageableUtils.sanitize(pageable, DEFAULT_COMMANDE_SORT, PageableUtils.COMMANDE_SORT_FIELDS);
        return ResponseEntity.ok(PageResponse.from(commandeClientService.listerToutes(safePageable)));
    }

    @GetMapping(value = "/client/{clientId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les commandes d'un client")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Commandes du client",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Client introuvable",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<PageResponse<CommandeClientResponse>> listerParClient(
            @PathVariable Long clientId,
            @PageableDefault(size = 20, sort = DEFAULT_COMMANDE_SORT, direction = Sort.Direction.DESC) Pageable pageable) {
        Pageable safePageable = PageableUtils.sanitize(pageable, DEFAULT_COMMANDE_SORT, PageableUtils.COMMANDE_SORT_FIELDS);
        return ResponseEntity.ok(PageResponse.from(commandeClientService.listerParClient(clientId, safePageable)));
    }

    @GetMapping(value = "/statut/{statut}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les commandes par statut")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Commandes filtrées par statut",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    public ResponseEntity<PageResponse<CommandeClientResponse>> listerParStatut(
            @PathVariable StatutCommande statut,
            @PageableDefault(size = 20, sort = DEFAULT_COMMANDE_SORT, direction = Sort.Direction.DESC) Pageable pageable) {
        Pageable safePageable = PageableUtils.sanitize(pageable, DEFAULT_COMMANDE_SORT, PageableUtils.COMMANDE_SORT_FIELDS);
        return ResponseEntity.ok(PageResponse.from(commandeClientService.listerParStatut(statut, safePageable)));
    }

    @GetMapping(value = "/numero/{numero}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Rechercher une commande par numéro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Commande trouvée",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommandeClientResponse.class))),
            @ApiResponse(responseCode = "404", description = "Commande introuvable",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<CommandeClientResponse> rechercherParNumero(@PathVariable String numero) {
        return ResponseEntity.ok(commandeClientService.rechercherParNumero(numero));
    }

    @PostMapping(value = "/{id}/confirmer", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Confirmer une commande")
    public ResponseEntity<CommandeClientResponse> confirmer(@PathVariable Long id) {
        return ResponseEntity.ok(commandeClientService.confirmer(id));
    }

    @PutMapping(value = "/{id}/statut", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Changer le statut d'une commande")
    public ResponseEntity<CommandeClientResponse> changerStatut(
            @PathVariable Long id,
            @RequestParam StatutCommande nouveauStatut) {
        return ResponseEntity.ok(commandeClientService.changerStatut(id, nouveauStatut));
    }

    @PostMapping(value = "/{id}/lignes", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Ajouter une ligne à la commande")
    public ResponseEntity<CommandeClientResponse> ajouterLigne(
            @PathVariable Long id,
            @Valid @RequestBody LigneCommandeRequest request) {
        return ResponseEntity.ok(commandeClientService.ajouterLigne(id, request));
    }

    @DeleteMapping(value = "/{id}/lignes/{ligneId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Supprimer une ligne d'une commande")
    public ResponseEntity<CommandeClientResponse> supprimerLigne(@PathVariable Long id, @PathVariable Long ligneId) {
        return ResponseEntity.ok(commandeClientService.supprimerLigne(id, ligneId));
    }
}
