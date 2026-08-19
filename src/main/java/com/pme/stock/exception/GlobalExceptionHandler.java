package com.pme.stock.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    // Permet de traiter handleNotFound.
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Ressource introuvable");
        pd.setType(URI.create("https://pme.com/errors/not-found"));
        pd.setProperty("timestamp", Instant.now());
        return problem(HttpStatus.NOT_FOUND, pd);
    }

    @ExceptionHandler(BusinessException.class)
    // Permet de traiter handleBusiness.
    public ResponseEntity<ProblemDetail> handleBusiness(BusinessException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Erreur métier");
        pd.setType(URI.create("https://pme.com/errors/business"));
        pd.setProperty("timestamp", Instant.now());
        return problem(HttpStatus.CONFLICT, pd);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    // Permet de traiter handleValidation.
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            errors.put(field, error.getDefaultMessage());
        });
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Données invalides");
        pd.setTitle("Erreur de validation");
        pd.setType(URI.create("https://pme.com/errors/validation"));
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("erreurs", errors);
        return problem(HttpStatus.BAD_REQUEST, pd);
    }

    @ExceptionHandler(AccessDeniedException.class)
    // Permet de traiter handleAccessDenied.
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Accès refusé : privilèges insuffisants");
        pd.setTitle("Accès refusé");
        pd.setType(URI.create("https://pme.com/errors/forbidden"));
        pd.setProperty("timestamp", Instant.now());
        return problem(HttpStatus.FORBIDDEN, pd);
    }

    @ExceptionHandler(BadCredentialsException.class)
    // Permet de traiter handleBadCredentials.
    public ResponseEntity<ProblemDetail> handleBadCredentials(BadCredentialsException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect");
        pd.setTitle("Identifiants invalides");
        pd.setType(URI.create("https://pme.com/errors/unauthorized"));
        pd.setProperty("timestamp", Instant.now());
        return problem(HttpStatus.UNAUTHORIZED, pd);
    }

    @ExceptionHandler(AuthenticationException.class)
    // Permet de traiter handleAuthentication.
    public ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Authentification requise");
        pd.setTitle("Non authentifié");
        pd.setType(URI.create("https://pme.com/errors/unauthorized"));
        pd.setProperty("timestamp", Instant.now());
        return problem(HttpStatus.UNAUTHORIZED, pd);
    }

    @ExceptionHandler({PropertyReferenceException.class, InvalidDataAccessApiUsageException.class})
    // Permet de traiter handleInvalidPageable.
    public ResponseEntity<ProblemDetail> handleInvalidPageable(Exception ex) {
        String detail = "Paramètre de pagination invalide";
        if (ex instanceof PropertyReferenceException pre) {
            detail = "Champ de tri invalide : '" + pre.getPropertyName()
                    + "'. Champs autorisés : code, raisonSociale, email, ville, createdAt";
        } else if (ex.getMessage() != null && ex.getMessage().contains("Could not resolve attribute")) {
            detail = "Champ de tri invalide. Champs autorisés : code, raisonSociale, email, ville, createdAt";
        }
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        pd.setTitle("Paramètre de pagination invalide");
        pd.setType(URI.create("https://pme.com/errors/validation"));
        pd.setProperty("timestamp", Instant.now());
        return problem(HttpStatus.BAD_REQUEST, pd);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    // Permet de traiter handleUnreadableBody.
    public ResponseEntity<ProblemDetail> handleUnreadableBody(HttpMessageNotReadableException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Corps de requête JSON manquant ou mal formé");
        pd.setTitle("Requête invalide");
        pd.setType(URI.create("https://pme.com/errors/validation"));
        pd.setProperty("timestamp", Instant.now());
        return problem(HttpStatus.BAD_REQUEST, pd);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    // Permet de traiter handleNoResourceFound.
    public ResponseEntity<ProblemDetail> handleNoResourceFound(NoResourceFoundException ex) {
        log.warn("Ressource non trouvée : {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Ressource non trouvée");
        pd.setTitle("Non trouvé");
        pd.setType(URI.create("https://pme.com/errors/not-found"));
        pd.setProperty("timestamp", Instant.now());
        return problem(HttpStatus.NOT_FOUND, pd);
    }

    @ExceptionHandler(Exception.class)
    // Permet de traiter handleGeneric.
    public ResponseEntity<ProblemDetail> handleGeneric(Exception ex) {
        log.error("Erreur inattendue", ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur interne s'est produite");
        pd.setTitle("Erreur interne");
        pd.setType(URI.create("https://pme.com/errors/internal"));
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("exceptionType", ex.getClass().getSimpleName());
        pd.setProperty("message", ex.getMessage());
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, pd);
    }

    // Permet de traiter problem.
    private ResponseEntity<ProblemDetail> problem(HttpStatus status, ProblemDetail body) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }
}
