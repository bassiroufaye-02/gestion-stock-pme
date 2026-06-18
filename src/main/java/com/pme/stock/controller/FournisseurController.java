package com.pme.stock.controller;

import com.pme.stock.dto.request.FournisseurRequest;
import com.pme.stock.dto.response.FournisseurResponse;
import com.pme.stock.dto.response.PageResponse;
import com.pme.stock.service.FournisseurService;
import com.pme.stock.util.PageableUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/fournisseurs")
@RequiredArgsConstructor
@Tag(name = "Fournisseurs", description = "API de gestion des fournisseurs")
@SecurityRequirement(name = "bearerAuth")
public class FournisseurController {

    private static final String DEFAULT_FOURNISSEUR_SORT = "raisonSociale";
    private final FournisseurService fournisseurService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Créer un nouveau fournisseur")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Fournisseur créé",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FournisseurResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
            @ApiResponse(responseCode = "409", description = "Code ou email déjà utilisé",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<FournisseurResponse> creer(@Valid @RequestBody FournisseurRequest request) {
        FournisseurResponse result = fournisseurService.creer(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(result.getId()).toUri();
        return ResponseEntity.created(location).body(result);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Récupérer un fournisseur par son ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fournisseur trouvé",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FournisseurResponse.class))),
            @ApiResponse(responseCode = "404", description = "Fournisseur introuvable",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<FournisseurResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(fournisseurService.trouverParId(id));
    }

    @GetMapping(value = "/code/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Récupérer un fournisseur par son code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fournisseur trouvé",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FournisseurResponse.class))),
            @ApiResponse(responseCode = "404", description = "Fournisseur introuvable",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<FournisseurResponse> trouverParCode(@PathVariable String code) {
        return ResponseEntity.ok(fournisseurService.trouverParCode(code));
    }

    @GetMapping(value = "/actifs", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les fournisseurs actifs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des fournisseurs actifs",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "Non authentifié",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<List<FournisseurResponse>> listerActifs() {
        return ResponseEntity.ok(fournisseurService.listerActifs());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Rechercher des fournisseurs",
            description = "Recherche par code ou raison sociale. Pagination : page, size. Tri optionnel : code, raisonSociale, email, ville, actif, createdAt.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Résultats de recherche paginés",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "Non authentifié",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<PageResponse<FournisseurResponse>> rechercher(
            @Parameter(description = "Terme de recherche", example = "FOUR-001")
            @RequestParam("q") String search,
            @PageableDefault(size = 20, sort = DEFAULT_FOURNISSEUR_SORT, direction = Sort.Direction.ASC) Pageable pageable) {
        Pageable safePageable = PageableUtils.sanitize(pageable, DEFAULT_FOURNISSEUR_SORT, PageableUtils.FOURNISSEUR_SORT_FIELDS);
        return ResponseEntity.ok(PageResponse.from(fournisseurService.rechercher(search, safePageable)));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Modifier un fournisseur")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fournisseur modifié",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FournisseurResponse.class))),
            @ApiResponse(responseCode = "404", description = "Fournisseur introuvable",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
            @ApiResponse(responseCode = "409", description = "Code ou email déjà utilisé",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<FournisseurResponse> modifier(@PathVariable Long id, @Valid @RequestBody FournisseurRequest request) {
        return ResponseEntity.ok(fournisseurService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Désactiver un fournisseur")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Fournisseur désactivé"),
            @ApiResponse(responseCode = "404", description = "Fournisseur introuvable",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
            @ApiResponse(responseCode = "403", description = "Rôle ADMIN requis",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        fournisseurService.desactiver(id);
        return ResponseEntity.noContent().build();
    }
}
