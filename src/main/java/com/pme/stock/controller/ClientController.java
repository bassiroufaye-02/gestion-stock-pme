package com.pme.stock.controller;

import com.pme.stock.dto.request.ClientRequest;
import com.pme.stock.dto.response.ClientResponse;
import com.pme.stock.dto.response.PageResponse;
import com.pme.stock.service.ClientService;
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

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Client", description = "API de gestion des clients")
@SecurityRequirement(name = "bearerAuth")
public class ClientController {

    private static final String DEFAULT_CLIENT_SORT = "raisonSociale";

    private final ClientService clientService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Créer un nouveau client")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Client créé",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ClientResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
            @ApiResponse(responseCode = "409", description = "Code ou email déjà utilisé",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<ClientResponse> creer(@Valid @RequestBody ClientRequest request) {
        ClientResponse client = clientService.creer(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(client.getId()).toUri();
        return ResponseEntity.created(location).body(client);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Récupérer un client par son ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client trouvé",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ClientResponse.class))),
            @ApiResponse(responseCode = "404", description = "Client introuvable",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<ClientResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.trouverParId(id));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les clients actifs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste paginée des clients actifs",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "Non authentifié",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<PageResponse<ClientResponse>> listerActifs(
            @PageableDefault(size = 20, sort = DEFAULT_CLIENT_SORT, direction = Sort.Direction.ASC) Pageable pageable) {
        Pageable safePageable = PageableUtils.sanitize(pageable, DEFAULT_CLIENT_SORT, PageableUtils.CLIENT_SORT_FIELDS);
        return ResponseEntity.ok(PageResponse.from(clientService.listerActifs(safePageable)));
    }

    @GetMapping(value = "/recherche", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Rechercher des clients",
            description = "Recherche par code ou raison sociale. Pagination : page, size. Tri optionnel : code, raisonSociale, email, ville, createdAt (ex. sort=code,asc).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Résultats de recherche paginés",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "Non authentifié",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<PageResponse<ClientResponse>> rechercher(
            @Parameter(description = "Texte à rechercher dans le code ou la raison sociale", example = "CLI-002")
            @RequestParam String search,
            @PageableDefault(size = 20, sort = DEFAULT_CLIENT_SORT, direction = Sort.Direction.ASC) Pageable pageable) {
        Pageable safePageable = PageableUtils.sanitize(pageable, DEFAULT_CLIENT_SORT, PageableUtils.CLIENT_SORT_FIELDS);
        return ResponseEntity.ok(PageResponse.from(clientService.rechercher(search, safePageable)));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Modifier un client")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client modifié",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ClientResponse.class))),
            @ApiResponse(responseCode = "404", description = "Client introuvable",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
            @ApiResponse(responseCode = "409", description = "Code ou email déjà utilisé",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<ClientResponse> modifier(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(clientService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Désactiver un client")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Client désactivé"),
            @ApiResponse(responseCode = "404", description = "Client introuvable",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
            @ApiResponse(responseCode = "403", description = "Rôle ADMIN requis",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        clientService.desactiver(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/tous", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister tous les clients (actifs et inactifs)",
               description = "Inclut les clients désactivés, contrairement à GET /clients qui ne montre que les actifs.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste paginée de tous les clients"),
            @ApiResponse(responseCode = "401", description = "Non authentifié",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<PageResponse<ClientResponse>> listerTous(
            @PageableDefault(size = 20, sort = DEFAULT_CLIENT_SORT, direction = Sort.Direction.ASC) Pageable pageable) {
        Pageable safePageable = PageableUtils.sanitize(pageable, DEFAULT_CLIENT_SORT, PageableUtils.CLIENT_SORT_FIELDS);
        return ResponseEntity.ok(PageResponse.from(clientService.listerTous(safePageable)));
    }

    @PostMapping(value = "/{id}/reactiver", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Réactiver un client désactivé")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client réactivé",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ClientResponse.class))),
            @ApiResponse(responseCode = "404", description = "Client introuvable",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
            @ApiResponse(responseCode = "403", description = "Rôle ADMIN ou GESTIONNAIRE requis",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public ResponseEntity<ClientResponse> reactiver(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.reactiver(id));
    }
}
