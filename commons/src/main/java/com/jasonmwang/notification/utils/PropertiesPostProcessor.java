package com.jasonmwang.notification.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PropertiesPostProcessor implements EnvironmentPostProcessor {
	public static final String ENCODED_SECRET_PREFIX = "encodedEnp:";
	public static final String CONVERT_ENCODED_KEYSTORE_PREFIX = "convertEncodedKeystoreEnp:";
	private ConfigurableEnvironment environment;

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		this.environment = environment;
		environment.getPropertySources().stream().filter(ps -> ps instanceof OriginTrackedMapPropertySource)
				.map(OriginTrackedMapPropertySource.class::cast).map(p -> this.decodeEncodedProps(p))
				.collect(Collectors.toList()).forEach(ps -> environment.getPropertySources().replace(ps.getName(), ps));
	}

	private OriginTrackedMapPropertySource decodeEncodedProps(
			final OriginTrackedMapPropertySource originTrackedMapPropertySource) {
		Map<String, Object> propertyMap = new HashMap<>(originTrackedMapPropertySource.getSource());
		propertyMap.entrySet().stream().filter(mapEntry -> mapEntry.getValue() instanceof OriginTrackedValue)
				.filter(mapEntry -> OriginTrackedValue.class.cast(mapEntry.getValue()).getValue() instanceof String)
				.filter(mapEntry -> OriginTrackedValue.class.cast(mapEntry.getValue()).getValue().toString()
						.startsWith(ENCODED_SECRET_PREFIX)
						|| OriginTrackedValue.class.cast(mapEntry.getValue()).getValue().toString()
								.startsWith(CONVERT_ENCODED_KEYSTORE_PREFIX))
				.collect(Collectors.toList()).forEach(mapEntry -> {
					String value = environment.getProperty(mapEntry.getKey());
					if (value.startsWith(ENCODED_SECRET_PREFIX)) {
						String removed = StringUtils.removeStart(value, ENCODED_SECRET_PREFIX);
						try {
							String decoded = new String(Base64.getDecoder().decode(removed));
							propertyMap.put(mapEntry.getKey(), decoded);
						} catch (Exception e) {
							log.error("failed to decode value: {}.", removed);
							throw e;
						}
					} else if (value.startsWith(CONVERT_ENCODED_KEYSTORE_PREFIX)) {
						String removed = StringUtils.removeStart(value, CONVERT_ENCODED_KEYSTORE_PREFIX);
						try {
							String resource = convertEncodedToResource(removed, "jks");
							propertyMap.put(mapEntry.getKey(), resource);
						} catch (IOException e) {
							log.error(e.getMessage(), e);
							throw new EnpTokenRetrievalException(
									"Failed to parse convertEncodedKeystoreEnp for key: " + mapEntry.getKey());
						}
					} else {
						propertyMap.put(mapEntry.getKey(), value);
					}
				});
		return new OriginTrackedMapPropertySource(originTrackedMapPropertySource.getName(),
				Collections.unmodifiableMap(propertyMap));
	}

	private static String convertEncodedToResource(final String encoded, String fileExt) throws IOException {
		File file = File.createTempFile("tempstore", "." + fileExt, null);
		Resource resource = new ByteArrayResource(encoded.getBytes());
		try (InputStream inputStream = resource.getInputStream()) {
			byte[] bytes = IOUtils.toByteArray(inputStream);
			try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
					Base64.getDecoder().decode(bytes));
					FileOutputStream fileOutputStream = new FileOutputStream(file);) {
				IOUtils.copy(byteArrayInputStream, fileOutputStream);
			}
		}
		return file.getAbsolutePath();
	}
}
