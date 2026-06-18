package com.pme.stock.controller;

import com.pme.stock.dto.request.CommandeFournisseurRequest;
import com.pme.stock.dto.request.LigneCommandeFournisseurRequest;
import com.pme.stock.dto.response.CommandeFournisseurResponse;
import com.pme.stock.dto.response.PageResponse;
import com.pme.stock.entity.StatutCommandeFournisseur;
import com.pme.stock.service.CommandeFournisseurService;
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
@RequestMapping("/api/v1/commandes-fournisseurs")
@RequiredArgsConstructor
@Tag(name = "Commandes Fournisseurs", description = "API de gestion des commandes fournisseurs")
@SecurityRequirement(name = "bearerAuth")
public class CommandeFournisseurController {

    private static final String DEFAULT_COMMANDE_FOURNISSEUR_SORT = "dateCommande";
    private final CommandeFournisseurService commandeFournisseurService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Créer une nouvelle commande fournisseur")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Commande créée",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommandeFournisseurResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Fournisseur ou produit introuvable",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<CommandeFournisseurResponse> creer(@Valid @RequestBody CommandeFournisseurRequest request) {
        CommandeFournisseurResponse result = commandeFournisseurService.creer(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(result.getId()).toUri();
        return ResponseEntity.created(location).body(result);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Récupérer une commande fournisseur par son ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Commande trouvée",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommandeFournisseurResponse.class))),
            @ApiResponse(responseCode = "404", description = "Commande introuvable",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<CommandeFournisseurResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(commandeFournisseurService.trouverParId(id));
    }

    @GetMapping(value = "/numero/{numero}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Récupérer une commande fournisseur par son numéro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Commande trouvée",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommandeFournisseurResponse.class))),
            @ApiResponse(responseCode = "404", description = "Commande introuvable",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<CommandeFournisseurResponse> trouverParNumero(@PathVariable String numero) {
        return ResponseEntity.ok(commandeFournisseurService.trouverParNumero(numero));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister toutes les commandes fournisseurs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste paginée des commandes",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "Non authentifié",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<PageResponse<CommandeFournisseurResponse>> listerToutes(
            @PageableDefault(size = 20, sort = DEFAULT_COMMANDE_FOURNISSEUR_SORT, direction = Sort.Direction.DESC) Pageable pageable) {
        Pageable safePageable = PageableUtils.sanitize(pageable, DEFAULT_COMMANDE_FOURNISSEUR_SORT, PageableUtils.COMMANDE_FOURNISSEUR_SORT_FIELDS);
        return ResponseEntity.ok(PageResponse.from(commandeFournisseurService.listerToutes(safePageable)));
    }

    @GetMapping(value = "/fournisseur/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les commandes d'un fournisseur")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Commandes du fournisseur",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "Non authentifié",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<PageResponse<CommandeFournisseurResponse>> listerParFournisseur(
            @PathVariable("id") Long fournisseurId,
            @PageableDefault(size = 20, sort = DEFAULT_COMMANDE_FOURNISSEUR_SORT, direction = Sort.Direction.DESC) Pageable pageable) {
        Pageable safePageable = PageableUtils.sanitize(pageable, DEFAULT_COMMANDE_FOURNISSEUR_SORT, PageableUtils.COMMANDE_FOURNISSEUR_SORT_FIELDS);
        return ResponseEntity.ok(PageResponse.from(commandeFournisseurService.listerParFournisseur(fournisseurId, safePageable)));
    }

    @GetMapping(value = "/statut/{statut}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les commandes par statut")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Commandes avec le statut spécifié",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "Non authentifié",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<PageResponse<CommandeFournisseurResponse>> listerParStatut(
            @PathVariable("statut") StatutCommandeFournisseur statut,
            @PageableDefault(size = 20, sort = DEFAULT_COMMANDE_FOURNISSEUR_SORT, direction = Sort.Direction.DESC) Pageable pageable) {
        Pageable safePageable = PageableUtils.sanitize(pageable, DEFAULT_COMMANDE_FOURNISSEUR_SORT, PageableUtils.COMMANDE_FOURNISSEUR_SORT_FIELDS);
        return ResponseEntity.ok(PageResponse.from(commandeFournisseurService.listerParStatut(statut, safePageable)));
    }

    @PostMapping(value = "/{id}/envoyer", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Envoyer une commande au fournisseur")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Commande envoyée",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommandeFournisseurResponse.class))),
            @ApiResponse(responseCode = "400", description = "État invalide",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Commande introuvable",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<CommandeFournisseurResponse> envoyer(@PathVariable Long id) {
        return ResponseEntity.ok(commandeFournisseurService.envoyer(id));
    }

    @PostMapping(value = "/{id}/receptionner", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Réceptionner une commande fournisseur")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Commande réceptionnée",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommandeFournisseurResponse.class))),
            @ApiResponse(responseCode = "400", description = "État invalide",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Commande introuvable",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<CommandeFournisseurResponse> receptionner(@PathVariable Long id) {
        return ResponseEntity.ok(commandeFournisseurService.receptionner(id));
    }

    @PostMapping(value = "/{id}/annuler", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Annuler une commande fournisseur")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Commande annulée",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommandeFournisseurResponse.class))),
            @ApiResponse(responseCode = "400", description = "Commande déjà réceptionnée",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Commande introuvable",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<CommandeFournisseurResponse> annuler(@PathVariable Long id) {
        return ResponseEntity.ok(commandeFournisseurService.annuler(id));
    }

    @PostMapping(value = "/{id}/lignes", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Ajouter une ligne à une commande fournisseur")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ligne ajoutée",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommandeFournisseurResponse.class))),
            @ApiResponse(responseCode = "400", description = "Commande non modifiable",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Commande ou produit introuvable",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<CommandeFournisseurResponse> ajouterLigne(
            @PathVariable Long id,
            @Valid @RequestBody LigneCommandeFournisseurRequest request) {
        return ResponseEntity.ok(commandeFournisseurService.ajouterLigne(id, request));
    }

    @DeleteMapping(value = "/{id}/lignes/{ligneId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Supprimer une ligne d'une commande fournisseur")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ligne supprimée",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommandeFournisseurResponse.class))),
            @ApiResponse(responseCode = "400", description = "Commande non modifiable",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Commande ou ligne introuvable",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<CommandeFournisseurResponse> supprimerLigne(
            @PathVariable Long id,
            @PathVariable Long ligneId) {
        return ResponseEntity.ok(commandeFournisseurService.supprimerLigne(id, ligneId));
    }
}
