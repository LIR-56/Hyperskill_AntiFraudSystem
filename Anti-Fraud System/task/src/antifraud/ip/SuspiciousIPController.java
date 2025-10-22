package antifraud.ip;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class SuspiciousIPController {

    public final SuspiciousIPService suspiciousIPService;

    public SuspiciousIPController(SuspiciousIPService suspiciousIPService) {
        this.suspiciousIPService = suspiciousIPService;
    }

    @PostMapping("/api/antifraud/suspicious-ip")
    public SuspiciousIP addSuspiciousIP(@RequestBody @Valid SuspiciousIP suspiciousIP) {
        return suspiciousIPService.add(suspiciousIP);
    }

    @DeleteMapping("/api/antifraud/suspicious-ip/{ip}")
    public Map<String, String> deleteSuspiciousIP(@PathVariable String ip) {
        suspiciousIPService.deleteIp(ip);
        return Map.of("status", String.format("IP %s successfully removed!", ip));
    }

    @GetMapping("/api/antifraud/suspicious-ip")
    public List<SuspiciousIP> getAllIPs() {
        return suspiciousIPService.getAll();
    }
}
