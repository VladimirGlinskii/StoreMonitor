package ru.vglinskii.storemonitor.baseapi.controller;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CashRegisterController {
    private final CashRegisterService cashRegisterService;

    public CashRegisterController(CashRegisterService cashRegisterService) {
        this.cashRegisterService = cashRegisterService;
    }

    @PostMapping
    public CashRegisterDtoResponse create(
            @RequestHeader("X-Store-Id") long storeId,
            @Valid @RequestBody CreateCashRegisterDtoRequest request
    ) {
        log.info("Creating cash register in store {}", storeId);
        return cashRegisterService.create(storeId, request);
    }

    @DeleteMapping("{id}")
    public void delete(
            @RequestHeader("X-Store-Id") long storeId,
            @PathVariable long id
    ) {
        log.info("Deleting cash register by id={} in store {}", id, storeId);
        cashRegisterService.delete(storeId, id);
    }

    @PostMapping("{id}/sessions")
    public void openSession(
            @RequestHeader("X-Store-Id") long storeId,
            @PathVariable long id,
            @Valid @RequestBody UpdateCashRegisterStatusDtoRequest request
    ) {
        log.info("Opening cash register {} in store {}", id, storeId);
        cashRegisterService.openSession(storeId, id, request);
    }

    @DeleteMapping("{id}/sessions")
    public void closeSession(
            @RequestHeader("X-Store-Id") long storeId,
            @PathVariable long id,
            @Valid @RequestBody UpdateCashRegisterStatusDtoRequest request
    ) {
        log.info("Closing cash register {} in store {}", id, storeId);
        cashRegisterService.closeSession(storeId, id, request);
    }

    @GetMapping("statuses")
    public List<CashRegisterStatusDtoResponse> getStatuses(
            @RequestHeader("X-Store-Id") long storeId
    ) {
        log.info("Received get cash registers statuses request for store {}", storeId);
        return cashRegisterService.getStatuses(storeId);
    }

    @GetMapping("work-summary")
    public CashRegistersWorkSummaryDtoResponse getWorkSummary(
            @RequestHeader("X-Store-Id") long storeId,
            @RequestParam() Instant from,
            @RequestParam() Instant to
    ) {
        log.info("Received get cash registers working summary request for store {}", storeId);
        return cashRegisterService.getWorkSummary(storeId, from, to);
    }
}
