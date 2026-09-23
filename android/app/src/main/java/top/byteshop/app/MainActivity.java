package top.byteshop.app;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import com.getcapacitor.BridgeActivity;
import java.util.concurrent.Executor;

public class MainActivity extends BridgeActivity {
    private boolean authenticated = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        );

        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        hideSystemBars();

        // شروع احراز هویت با اثر انگشت
        authenticateWithBiometric();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemBars();
        }
    }

    private void hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        }
    }

    private void authenticateWithBiometric() {
        BiometricManager biometricManager = BiometricManager.from(this);

        int canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
            | BiometricManager.Authenticators.BIOMETRIC_WEAK
        );

        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            // اثر انگشت در دسترس نیست - اجازه بده وارد شه
            authenticated = true;
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(this);

        BiometricPrompt biometricPrompt = new BiometricPrompt(
            this,
            executor,
            new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    authenticated = true;
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    // دوباره prompt ظاهر می‌شه
                }

                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    // اگه کاربر کنسل کرد یا خطا داد، اپ بسته شه
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                        || errorCode == BiometricPrompt.ERROR_USER_CANCELED
                        || errorCode == BiometricPrompt.ERROR_CANCELED) {
                        finishAffinity();
                    }
                }
            }
        );

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
            .setTitle("ورود به ByteShop")
            .setSubtitle("اثر انگشت خود را اسکن کنید")
            .setNegativeButtonText("لغو")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
                | BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            .build();

        biometricPrompt.authenticate(promptInfo);
    }
}
