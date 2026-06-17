package com.globalstock.webapi.controller;

import com.globalstock.webapi.common.ApiResponse;
import com.globalstock.webapi.model.dto.ApiTokenDTO;
import com.globalstock.webapi.model.dto.TokenUsageDTO;
import com.globalstock.webapi.service.TokenService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tokens/v1")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping
    public ApiResponse<ApiTokenDTO> generate() {
        return ApiResponse.success(tokenService.generateForCurrentUser());
    }

    @GetMapping
    public ApiResponse<List<ApiTokenDTO>> list() {
        return ApiResponse.success(tokenService.listForCurrentUser());
    }

    @DeleteMapping("/{tokenId}")
    public ApiResponse<Void> revoke(@PathVariable String tokenId) {
        tokenService.revoke(tokenId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{tokenId}/rotate")
    public ApiResponse<ApiTokenDTO> rotate(@PathVariable String tokenId) {
        return ApiResponse.success(tokenService.rotate(tokenId));
    }

    @PostMapping("/{tokenId}/refresh-permissions")
    public ApiResponse<ApiTokenDTO> refreshPermissions(@PathVariable String tokenId) {
        return ApiResponse.success(tokenService.refreshPermissions(tokenId));
    }

    @GetMapping("/{tokenId}/usage")
    public ApiResponse<TokenUsageDTO> usage(@PathVariable String tokenId) {
        return ApiResponse.success(tokenService.getTokenUsage(tokenId));
    }
}
