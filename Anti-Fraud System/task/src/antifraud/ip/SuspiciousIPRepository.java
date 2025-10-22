package antifraud.ip;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SuspiciousIPRepository extends JpaRepository<SuspiciousIP, Long> {
    SuspiciousIP findByIp(String ip);
}
