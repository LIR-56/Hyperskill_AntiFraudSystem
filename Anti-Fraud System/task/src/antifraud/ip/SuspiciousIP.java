package antifraud.ip;

import antifraud.exceptions.WrongIpFormatException;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.regex.Pattern;

@Entity
@Table(name = "IPs", indexes = @Index(columnList = "ip_address"))
public class SuspiciousIP {

    @Transient
    private static final Pattern pattern = Pattern.compile("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$");

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "ip_id")
    private long id;

    @Column(name = "ip_address", unique = true)
    @Nonnull
    @NotBlank
    private String ip;

    public SuspiciousIP() {
    }

    public void validate() {
        if (ip == null || !pattern.matcher(ip).matches()) {
            throw new WrongIpFormatException();
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Nonnull
    public String getIp() {
        return ip;
    }

    public void setIp(@Nonnull String ip) {
        this.ip = ip;
    }
}
