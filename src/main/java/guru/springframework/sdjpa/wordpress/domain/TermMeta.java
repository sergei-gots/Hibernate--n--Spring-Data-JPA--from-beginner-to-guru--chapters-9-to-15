package guru.springframework.sdjpa.wordpress.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Created by sergei on 12/04/2025
 */
@Entity
@Table(name = "wp_termmeta")
public class TermMeta extends Meta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meta_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "term_id")
    private Term term;

}
