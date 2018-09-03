package android.support.v4.app;

/**
 * Due to Google Play Services still depending on the old support libraries, and the old libraries
 * not being compile-time compatible with the new libraries, we have 'ported' the necessary classes
 * so that everything runs the latest and greatest code.
 *
 * If a runtime crash occurs, we may have missed a class that we had to port.
 *
 * Note that if/when Google Play Services updates to the new libraries, this compat files simply
 * won't be called any longer.
 */
public abstract class FragmentActivity extends androidx.fragment.app.FragmentActivity {
}
