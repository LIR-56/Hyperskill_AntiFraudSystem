package antifraud.ip;

import antifraud.exceptions.DuplicateIpException;
import antifraud.exceptions.SuspiciousIPNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SuspiciousIPService {

    private final SuspiciousIPRepository suspiciousIPRepository;

    @Autowired
    public SuspiciousIPService(SuspiciousIPRepository suspiciousIPRepository) {
        this.suspiciousIPRepository = suspiciousIPRepository;
    }

    public SuspiciousIP add(SuspiciousIP suspiciousIP) {
        suspiciousIP.validate();
        if (suspiciousIPRepository.findByIp(suspiciousIP.getIp()) != null) {
            throw new DuplicateIpException();
        }
        return suspiciousIPRepository.save(suspiciousIP);
    }

    public void deleteIp(String ipv4) {
        var x = new SuspiciousIP();
        x.setIp(ipv4);
        x.validate();
        var element = suspiciousIPRepository.findByIp(ipv4);
        if (element == null) {
            throw new SuspiciousIPNotFoundException();
        }
        suspiciousIPRepository.delete(element);
    }

    public List<SuspiciousIP> getAll() {
        return suspiciousIPRepository.findAll(Sort.by(Sort.Order.by("ip")));
    }

    public boolean isIpSuspicious(String ip) {
        return suspiciousIPRepository.findByIp(ip) != null;
    }
}
