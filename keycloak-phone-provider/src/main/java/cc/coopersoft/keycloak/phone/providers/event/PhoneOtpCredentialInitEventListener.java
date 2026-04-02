package cc.coopersoft.keycloak.phone.providers.event;

import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialModel;
import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialProvider;
import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialProviderFactory;
import org.jboss.logging.Logger;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class PhoneOtpCredentialInitEventListener implements EventListenerProvider {

    private static final Logger logger = Logger.getLogger(PhoneOtpCredentialInitEventListener.class);

    private final KeycloakSession session;

    public PhoneOtpCredentialInitEventListener(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {
        if (!EventType.REGISTER.equals(event.getType())) return;

        String userId = event.getUserId();
        String realmId = event.getRealmId();
        if (userId == null || realmId == null) return;

        RealmModel realm = session.realms().getRealm(realmId);
        if (realm == null) return;

        UserModel user = session.users().getUserById(realm, userId);
        if (user == null) return;

        initPhoneOtpCredential(realm, user);
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        if (!OperationType.CREATE.equals(adminEvent.getOperationType())) return;
        if (!ResourceType.USER.equals(adminEvent.getResourceType())) return;

        // resourcePath = "users/{userId}"
        String resourcePath = adminEvent.getResourcePath();
        if (resourcePath == null || !resourcePath.startsWith("users/")) return;

        String userId = resourcePath.substring("users/".length());
        String realmId = adminEvent.getRealmId();

        RealmModel realm = session.realms().getRealm(realmId);
        if (realm == null) return;

        UserModel user = session.users().getUserById(realm, userId);
        if (user == null) return;

        initPhoneOtpCredential(realm, user);
    }

    private void initPhoneOtpCredential(RealmModel realm, UserModel user) {
        boolean alreadyConfigured = user.credentialManager()
                .getStoredCredentialsByTypeStream(PhoneOtpCredentialModel.TYPE)
                .findAny()
                .isPresent();
        if (alreadyConfigured) {
            logger.debugf("User %s already has phone-otp credential, skipping init", user.getId());
            return;
        }

        String phoneNumber = user.getFirstAttribute("phoneNumber");
        if (phoneNumber == null || phoneNumber.isBlank()) {
            logger.debugf("User %s has no phoneNumber attribute, skipping phone-otp credential init", user.getId());
            return;
        }

        PhoneOtpCredentialProvider ocp = (PhoneOtpCredentialProvider) session
                .getProvider(CredentialProvider.class, PhoneOtpCredentialProviderFactory.PROVIDER_ID);

        // expires=0 → isSecretInvalid()=true → khi login sẽ fallback sang PhoneVerificationCodeProvider
        ocp.createCredential(realm, user, PhoneOtpCredentialModel.create(phoneNumber, null, 0));
        logger.infof("Phone-OTP credential initialized for user %s with phone %s", user.getId(), phoneNumber);
    }

    @Override
    public void close() {
    }
}