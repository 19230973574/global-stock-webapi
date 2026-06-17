package com.globalstock.webapi.controller;

import com.globalstock.webapi.common.ApiResponse;
import com.globalstock.webapi.model.dto.KlineBarDTO;
import com.globalstock.webapi.model.dto.QuantScreenDTO;
import com.globalstock.webapi.model.dto.QuantScreenPageDTO;
import com.globalstock.webapi.service.QuantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/quant/v1")
public class QuantController {

    private final QuantService quantService;

    public QuantController(QuantService quantService) {
        this.quantService = quantService;
    }

    @GetMapping("/us/screen")
    public ApiResponse<QuantScreenDTO> screenUsMarket(@RequestParam(required = false) Integer days,
                                                      @RequestParam(required = false) Integer limit) {
        return ApiResponse.success(quantService.screenUsMarket(days, limit));
    }

    @GetMapping("/us/screen/page")
    public ApiResponse<QuantScreenPageDTO> screenUsMarketPage(
            @RequestParam(defaultValue = "new_high") String signalType,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) String period,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Double minMarketCap,
            @RequestParam(required = false) Double maxMarketCap,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        return ApiResponse.success(quantService.screenUsMarketPage(
                signalType, days, period, page, pageSize, code, minMarketCap, maxMarketCap,
                sortBy, sortOrder, true));
    }

    @GetMapping("/us/kline")
    public ApiResponse<List<KlineBarDTO>> getUsKline(@RequestParam String code,
                                                     @RequestParam(required = false) Integer limit) {
        return ApiResponse.success(quantService.getUsHistoryBars(code, limit, true));
    }

    @GetMapping("/us/windows")
    public ApiResponse<List<Integer>> supportedWindows() {
        return ApiResponse.success(quantService.supportedWindows());
    }
}
