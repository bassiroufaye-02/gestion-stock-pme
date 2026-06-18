package com.pme.stock.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler - Tests unitaires")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Test
    @DisplayName("✅ handleNotFound - Retourne NOT_FOUND 404")
    void testHandleNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Ressource non trouvée");
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Ressource introuvable");
        assertThat(response.getBody().getDetail()).isEqualTo("Ressource non trouvée");
    }

    @Test
    @DisplayName("✅ handleBusiness - Retourne CONFLICT 409")
    void testHandleBusiness() {
        BusinessException ex = new BusinessException("Erreur de règle métier");
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleBusiness(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Erreur métier");
        assertThat(response.getBody().getDetail()).isEqualTo("Erreur de règle métier");
    }

    @Test
    @DisplayName("✅ handleValidation - Retourne BAD_REQUEST 400 avec la liste des erreurs")
    void testHandleValidation() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("clientRequest", "email", "L'email est requis");

        given(ex.getBindingResult()).willReturn(bindingResult);
        given(bindingResult.getAllErrors()).willReturn(List.of(fieldError));

        ResponseEntity<ProblemDetail> response = exceptionHandler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Erreur de validation");

        Map<String, Object> properties = response.getBody().getProperties();
        assertThat(properties).containsKey("erreurs");
        @SuppressWarnings("unchecked")
        Map<String, String> errorsMap = (Map<String, String>) properties.get("erreurs");
        assertThat(errorsMap).containsEntry("email", "L'email est requis");
    }

    @Test
    @DisplayName("✅ handleAccessDenied - Retourne FORBIDDEN 403")
    void testHandleAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Forbidden");
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleAccessDenied(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Accès refusé");
    }

    @Test
    @DisplayName("✅ handleBadCredentials - Retourne UNAUTHORIZED 401")
    void testHandleBadCredentials() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleBadCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Identifiants invalides");
    }

    @Test
    @DisplayName("✅ handleAuthentication - Retourne UNAUTHORIZED 401")
    void testHandleAuthentication() {
        InsufficientAuthenticationException ex = new InsufficientAuthenticationException("Unauthorized");
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleAuthentication(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Non authentifié");
    }

    @Test
    @DisplayName("✅ handleInvalidPageable - PropertyReferenceException - Retourne BAD_REQUEST 400")
    void testHandleInvalidPageablePropertyReference() {
        PropertyReferenceException ex = mock(PropertyReferenceException.class);
        given(ex.getPropertyName()).willReturn("invalidField");

        ResponseEntity<ProblemDetail> response = exceptionHandler.handleInvalidPageable(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).contains("Champ de tri invalide : 'invalidField'");
    }

    @Test
    @DisplayName("✅ handleInvalidPageable - InvalidDataAccessApiUsageException - Retourne BAD_REQUEST 400")
    void testHandleInvalidPageableDataAccess() {
        InvalidDataAccessApiUsageException ex = new InvalidDataAccessApiUsageException("Could not resolve attribute");

        ResponseEntity<ProblemDetail> response = exceptionHandler.handleInvalidPageable(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).contains("Champ de tri invalide");
    }

    @Test
    @DisplayName("✅ handleUnreadableBody - HttpMessageNotReadableException - Retourne BAD_REQUEST 400")
    void testHandleUnreadableBody() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

        ResponseEntity<ProblemDetail> response = exceptionHandler.handleUnreadableBody(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Requête invalide");
    }

    @Test
    @DisplayName("✅ handleNoResourceFound - NoResourceFoundException - Retourne NOT_FOUND 404")
    void testHandleNoResourceFound() {
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/non-existent");

        ResponseEntity<ProblemDetail> response = exceptionHandler.handleNoResourceFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Non trouvé");
    }

    @Test
    @DisplayName("✅ handleGeneric - Exception - Retourne INTERNAL_SERVER_ERROR 500")
    void testHandleGeneric() {
        Exception ex = new Exception("Unexpected crash");

        ResponseEntity<ProblemDetail> response = exceptionHandler.handleGeneric(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Erreur interne");
    }
}
