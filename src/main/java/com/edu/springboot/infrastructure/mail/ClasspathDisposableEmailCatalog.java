package com.edu.springboot.infrastructure.mail;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.edu.springboot.domain.member.DisposableEmailCatalog;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ClasspathDisposableEmailCatalog implements DisposableEmailCatalog {

	private static final String RESOURCE = "/security/disposable-email-domains.txt";

	private final Set<String> domains;

	public ClasspathDisposableEmailCatalog() {
		this.domains = load();
		log.info("Loaded {} disposable email domains", domains.size());
	}

	@Override
	public boolean blocked(String domain) {
		if (domain == null || domain.isBlank()) {
			return false;
		}
		String current = domain.trim().toLowerCase(Locale.ROOT);
		while (!current.isEmpty()) {
			if (domains.contains(current)) {
				return true;
			}
			int dot = current.indexOf('.');
			if (dot < 0 || dot == current.length() - 1) {
				return false;
			}
			current = current.substring(dot + 1);
		}
		return false;
	}

	private Set<String> load() {
		Set<String> loaded = new HashSet<>();
		try (InputStream in = getClass().getResourceAsStream(RESOURCE)) {
			if (in == null) {
				log.warn("Disposable email list not found: {}", RESOURCE);
				return loaded;
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			String line;
			while ((line = reader.readLine()) != null) {
				String domain = line.trim().toLowerCase(Locale.ROOT);
				if (domain.isEmpty() || domain.startsWith("#")) {
					continue;
				}
				loaded.add(domain);
			}
		} catch (Exception ex) {
			log.warn("Failed to load disposable email list", ex);
		}
		return Set.copyOf(loaded);
	}
}
