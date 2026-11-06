package id.ac.upnvj.foundly.controller;

import id.ac.upnvj.foundly.dto.*;
import id.ac.upnvj.foundly.security.FoundlyUserPrincipal;
import id.ac.upnvj.foundly.service.PenggunaService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.*;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final PenggunaService penggunaService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;

    public AuthController(PenggunaService penggunaService,
                          AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository,
                          SessionAuthenticationStrategy sessionAuthenticationStrategy) {
        this.penggunaService = penggunaService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
    }

    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken token) {
        return new CsrfResponse(token.getHeaderName(), token.getParameterName(), token.getToken());
    }

    @PostMapping("/register")
    public ResponseEntity<PenggunaResponse> registrasi(@Valid @RequestBody RegistrasiRequest request) {
        PenggunaResponse pengguna = penggunaService.registrasi(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(pengguna);
    }

    @PostMapping("/login")
    public PenggunaResponse login(@Valid @RequestBody LoginRequest request,
                                  HttpServletRequest servletRequest,
                                  HttpServletResponse servletResponse) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
        sessionAuthenticationStrategy.onAuthentication(authentication, servletRequest, servletResponse);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, servletRequest, servletResponse);

        FoundlyUserPrincipal principal = (FoundlyUserPrincipal) authentication.getPrincipal();
        return new PenggunaResponse(principal.id(), principal.nama(), principal.email(), principal.peran());
    }

    @GetMapping("/me")
    public PenggunaResponse akunAktif(@AuthenticationPrincipal FoundlyUserPrincipal principal) {
        return new PenggunaResponse(principal.id(), principal.nama(), principal.email(), principal.peran());
    }
}
