package ee.sk.smartid;

import ee.sk.smartid.rest.SessionStatusPoller;
import ee.sk.smartid.rest.SmartIdConnectorSpy;
import ee.sk.smartid.rest.dao.SessionSignature;
import ee.sk.smartid.rest.dao.SessionStatus;
import ee.sk.smartid.rest.dao.SignatureSessionResponse;
import org.junit.Before;
import org.junit.Test;

import static ee.sk.smartid.DummyData.createSessionEndResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SignatureRequestBuilderTest {

  private SmartIdConnectorSpy connector;
  private SessionStatusPoller sessionStatusPoller;

  @Before
  public void setUp() throws Exception {
    connector = new SmartIdConnectorSpy();
    sessionStatusPoller = new SessionStatusPoller(connector);
    connector.signatureSessionResponseToRespond = createDummySignatureSessionResponse();
  }

  @Test
  public void sign() throws Exception {
    connector.sessionStatusToRespond = createDummySessionStatusResponse();
    SignatureRequestBuilder builder = new SignatureRequestBuilder(connector, sessionStatusPoller);
    SignableHash hashToSign = new SignableHash();
    hashToSign.setHashType("SHA256");
    hashToSign.setHashInBase64("0nbgC2fVdLVQFZJdBbmG7oPoElpCYsQMtrY0c0wKYRg=");
    SmartIdSignature signature = builder
        .withRelyingPartyUUID("relying-party-uuid")
        .withRelyingPartyName("relying-party-name")
        .withCertificateLevel("ADVANCED")
        .withHash(hashToSign)
        .withDocumentNumber("PNOEE-31111111111")
        .sign();
    assertCorrectSignatureRequestMade();
    assertCorrectSessionRequestMade();
    assertSignatureCorrect(signature);
  }

  private void assertCorrectSignatureRequestMade() {
    assertEquals("PNOEE-31111111111", connector.documentNumberUsed);
    assertEquals("relying-party-uuid", connector.signatureSessionRequestUsed.getRelyingPartyUUID());
    assertEquals("relying-party-name", connector.signatureSessionRequestUsed.getRelyingPartyName());
    assertEquals("ADVANCED", connector.signatureSessionRequestUsed.getCertificateLevel());
    assertEquals("SHA256", connector.signatureSessionRequestUsed.getHashType());
    assertEquals("0nbgC2fVdLVQFZJdBbmG7oPoElpCYsQMtrY0c0wKYRg=", connector.signatureSessionRequestUsed.getHash());
  }

  private void assertCorrectSessionRequestMade() {
    assertEquals("97f5058e-e308-4c83-ac14-7712b0eb9d86", connector.sessionIdUsed);
  }

  private void assertSignatureCorrect(SmartIdSignature signature) {
    assertNotNull(signature);
    assertEquals("luvjsi1+1iLN9yfDFEh/BE8h", signature.getValueInBase64());
    assertEquals("sha256WithRSAEncryption", signature.getAlgorithmName());
    assertEquals("PNOEE-31111111111", signature.getDocumentNumber());
  }

  private SignatureSessionResponse createDummySignatureSessionResponse() {
    SignatureSessionResponse response = new SignatureSessionResponse();
    response.setSessionId("97f5058e-e308-4c83-ac14-7712b0eb9d86");
    return response;
  }

  private SessionStatus createDummySessionStatusResponse() {
    SessionStatus status = new SessionStatus();
    status.setState("COMPLETE");
    status.setResult(createSessionEndResult());
    SessionSignature signature = new SessionSignature();
    signature.setValueInBase64("luvjsi1+1iLN9yfDFEh/BE8h");
    signature.setAlgorithm("sha256WithRSAEncryption");
    status.setSignature(signature);
    return status;
  }
}