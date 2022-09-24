package com.github.mrgatto.enclave.kms;

import com.github.mrgatto.model.AWSCredential;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Default KMS client implementation that use provided KMSTools binary.
 * <p>
 * More info at https://github.com/aws/aws-nitro-enclaves-sdk-c/blob/main/docs/kmstool.md
 *
 */
public class DefaultKMSClient implements KMSClient {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultKMSClient.class);

	private static final String GENKEY_CMD = "genkey";
	private static final String DECRYPT_CMD = "decrypt";
	private static final String GENRAND_CMD = "genrand";

	@Value("${nitro.enclave.kmstool-enclave-cli:/app/kmstool_enclave_cli}")
	private String kmsToolEnvlaveCli;

	@Override
	public String decrypt(AWSCredential credential, String content) {
		assertCredentail(credential);
		if (StringUtils.isEmpty(content)) {
			return content;
		}
		try {
			LOG.info("Executing kmstool_enclave_cli ({})...", this.kmsToolEnvlaveCli);
			String[] cmd = new String[] {
					this.kmsToolEnvlaveCli,
					DECRYPT_CMD,
					"--region", credential.getRegion(),
					"--aws-access-key-id", credential.getAccessKeyId(),
					"--aws-secret-access-key", credential.getSecretAccessKey(),
					"--aws-session-token", credential.getSessionToken(),
					"--ciphertext", content
			};
			ProcessBuilder builder = new ProcessBuilder(Arrays.asList(cmd));
			builder.inheritIO().redirectOutput(ProcessBuilder.Redirect.PIPE);
			Process process = builder.start();
			String procOutput = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
			process.waitFor();
			LOG.debug("KMSTool output: {}", procOutput);
			return procOutput;

		} catch (Exception e) {
			LOG.error("Cannot execute " + this.kmsToolEnvlaveCli, e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public String generateDataKey(AWSCredential credential, String keyId) {
		assertCredentail(credential);
		Assert.notNull(keyId, "keyId parameter must not be null");
		try {
			String[] cmd = new String[] {
					this.kmsToolEnvlaveCli,
					GENKEY_CMD,
					"--region", credential.getRegion(),
					"--aws-access-key-id", credential.getAccessKeyId(),
					"--aws-secret-access-key", credential.getSecretAccessKey(),
					"--aws-session-token", credential.getSessionToken(),
					"--key-id", keyId,
					"--key-spec", "AES-256"
			};

			ProcessBuilder builder = new ProcessBuilder(Arrays.asList(cmd));
			builder.inheritIO().redirectOutput(ProcessBuilder.Redirect.PIPE);
			Process process = builder.start();
			String procOutput = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
			process.waitFor();
			LOG.debug("KMSTool output: {}", procOutput);
			return procOutput;
		} catch (Exception e) {
			LOG.error("Cannot execute " + this.kmsToolEnvlaveCli, e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public String generateRandom(AWSCredential credential, int numberOfBytes) {
		assertCredentail(credential);
		if (numberOfBytes < 1 || numberOfBytes > 1024) {
			throw new RuntimeException("number of bytes can not less than 1 and greater than 1024");
		}
		try {
			String[] cmd = new String[]{
					this.kmsToolEnvlaveCli,
					GENRAND_CMD,
					"--region", credential.getRegion(),
					"--aws-access-key-id", credential.getAccessKeyId(),
					"--aws-secret-access-key", credential.getSecretAccessKey(),
					"--aws-session-token", credential.getSessionToken(),
					"--number-of-bytes", String.valueOf(numberOfBytes)
			};

			ProcessBuilder builder = new ProcessBuilder(Arrays.asList(cmd));
			builder.inheritIO().redirectOutput(ProcessBuilder.Redirect.PIPE);
			Process process = builder.start();
			String procOutput = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
			process.waitFor();
			LOG.debug("KMSTool output: {}", procOutput);
			return procOutput;
		} catch (Exception e) {
			LOG.error("Cannot execute " + this.kmsToolEnvlaveCli, e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private void assertCredentail(AWSCredential credential) {
		Assert.notNull(credential, "credential must not be null");
		Assert.notNull(credential.getAccessKeyId(), "credential accessKeyId must not be null");
		Assert.notNull(credential.getSecretAccessKey(), "credential secretAccessKey must not be null");
		Assert.notNull(credential.getSessionToken(), "credential sessionToken must not be null");
		Assert.notNull(credential.getRegion(), "credential region must not be null");
	}
}
