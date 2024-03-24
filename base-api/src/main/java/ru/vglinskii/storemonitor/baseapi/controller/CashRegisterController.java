package ru.vglinskii.storemonitor.baseapi.controller;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegisterDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegisterStatusDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegistersWorkSummaryDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CreateCashRegisterDtoRequest;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.UpdateCashRegisterStatusDtoRequest;
import ru.vglinskii.storemonitor.baseapi.service.CashRegisterService;

@RestController
@RequestMapping("/api/cash-registers")
public class CashRegisterController {
    private final CashRegisterService cashRegisterService;

    public CashRegisterController(CashRegisterService cashRegisterService) {
        this.cashRegisterService = cashRegisterService;
    }

    @PostMapping
    public CashRegisterDtoResponse create(
            @RequestHeader("storeId") long storeId,
            @Valid @RequestBody CreateCashRegisterDtoRequest request
    ) {
        return cashRegisterService.create(storeId, request);
    }

    @DeleteMapping("{id}")
    public void delete(
            @RequestHeader("storeId") long storeId,
            @PathVariable long id
    ) {
        cashRegisterService.delete(storeId, id);
    }

    @PostMapping("{id}/sessions")
    public void openSession(
            @RequestHeader("storeId") long storeId,
            @PathVariable long id,
            @Valid @RequestBody UpdateCashRegisterStatusDtoRequest request
    ) {
        cashRegisterService.openSession(storeId, id, request);
    }

    @DeleteMapping("{id}/sessions")
    public void closeSession(
            @RequestHeader("storeId") long storeId,
            @PathVariable long id,
            @Valid @RequestBody UpdateCashRegisterStatusDtoRequest request
    ) {
        cashRegisterService.closeSession(storeId, id, request);
    }

    @GetMapping("statuses")
    public List<CashRegisterStatusDtoResponse> getStatuses(@RequestHeader("storeId") long storeId) {
        return cashRegisterService.getStatuses(storeId);
    }

    @GetMapping("work-summary")
    public CashRegistersWorkSummaryDtoResponse getWorkSummary(
            @RequestHeader() long storeId,
            @RequestParam() LocalDateTime from,
            @RequestParam() LocalDateTime to
    ) {
        return cashRegisterService.getWorkSummary(storeId, from, to);
    }
}
