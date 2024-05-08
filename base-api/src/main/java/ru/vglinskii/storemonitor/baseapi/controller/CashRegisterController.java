package ru.vglinskii.storemonitor.baseapi.controller;

import jakarta.validation.Valid;
import java.time.Instant;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegisterDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegistersStatusesDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegistersWorkSummaryDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CreateCashRegisterDtoRequest;
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
            @Valid @RequestBody CreateCashRegisterDtoRequest request
    ) {
        return cashRegisterService.create(request);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable long id) {
        cashRegisterService.delete(id);
    }

    @PostMapping("{id}/sessions")
    public void openSession(@PathVariable long id) {
        cashRegisterService.openSession(id);
    }

    @DeleteMapping("{id}/sessions")
    public void closeSession(@PathVariable long id) {
        cashRegisterService.closeSession(id);
    }

    @GetMapping("statuses")
    public CashRegistersStatusesDtoResponse getStatuses() {
        return cashRegisterService.getStatuses();
    }

    @GetMapping("work-summary")
    public CashRegistersWorkSummaryDtoResponse getWorkSummary(
            @RequestParam() Instant from,
            @RequestParam() Instant to
    ) {
        return cashRegisterService.getWorkSummary(from, to);
    }
}
