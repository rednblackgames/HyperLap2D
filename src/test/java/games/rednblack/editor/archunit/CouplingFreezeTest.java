package games.rednblack.editor.archunit;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Guard rails for the .view refactor (plan Phase 0). Freezes the current
 * coupling counts so the refactor can only improve them: any NEW occurrence of
 * a forbidden pattern fails the build. As each phase removes call sites, lower
 * the FROZEN_* cap to ratchet the invariant down.
 *
 * This is a source-text ratchet (scans {@code src/main/java}) rather than
 * ArchUnit bytecode analysis, because ArchUnit's classpath/import scanning
 * returns 0 classes under this Java 25 / Gradle 9.1 classloader. Textual
 * counting is conservative (it may over-count inside comments/strings) which is
 * safe for a ratchet: it only ever forbids new occurrences.
 */
public class CouplingFreezeTest {

    /** Frozen caps (ratchet). Comment-stripped baseline captured 2026-07-20. Lower only after removing call sites. */
    private static final long FROZEN_SANDBOX_GETINSTANCE = 275L;   // view + controller + proxy + utils
    private static final long FROZEN_FACADE_GETINSTANCE_VIEW = 145L;        // view only
    private static final long FROZEN_SANDBOX_COMPONENT_RETRIEVER_VIEW = 159L; // view only

    private static final String EDITOR_SRC = "src/main/java/games/rednblack/editor";

    @Test
    public void noNewSandboxGetInstanceCalls() throws Exception {
        long count = 0;
        count += countOccurrences(new File(EDITOR_SRC, "view"), "Sandbox.getInstance()");
        count += countOccurrences(new File(EDITOR_SRC, "controller"), "Sandbox.getInstance()");
        count += countOccurrences(new File(EDITOR_SRC, "proxy"), "Sandbox.getInstance()");
        count += countOccurrences(new File(EDITOR_SRC, "utils"), "Sandbox.getInstance()");
        assertUnderFrozen(count, FROZEN_SANDBOX_GETINSTANCE,
                "Sandbox.getInstance() across editor view+controller+proxy+utils");
    }

    @Test
    public void noNewFacadeGetInstanceInView() throws Exception {
        long count = countOccurrences(new File(EDITOR_SRC, "view"), "Facade.getInstance()");
        assertUnderFrozen(count, FROZEN_FACADE_GETINSTANCE_VIEW,
                "Facade.getInstance() within editor view");
    }

    @Test
    public void noNewSandboxComponentRetrieverInView() throws Exception {
        long count = countOccurrences(new File(EDITOR_SRC, "view"), "SandboxComponentRetriever.");
        assertUnderFrozen(count, FROZEN_SANDBOX_COMPONENT_RETRIEVER_VIEW,
                "SandboxComponentRetriever. within editor view");
    }

    private static long countOccurrences(File root, String token) throws Exception {
        if (!root.isDirectory()) return 0;
        long[] total = {0};
        List<File> files = new ArrayList<>();
        collectJavaFiles(root, files);
        for (File f : files) {
            String src = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            // Strip comments so the ratchet counts actual code, not javadoc/comment
            // mentions of the pattern (e.g. "{@code Sandbox.getInstance()}").
            String stripped = src
                    .replaceAll("/\\*[\\s\\S]*?\\*/", " ")  // block comments
                    .replaceAll("//[^\\n]*", " ");           // line comments
            int idx = 0;
            while ((idx = stripped.indexOf(token, idx)) >= 0) {
                total[0]++;
                idx += token.length();
            }
        }
        return total[0];
    }

    private static void collectJavaFiles(File dir, List<File> out) {
        File[] children = dir.listFiles();
        if (children == null) return;
        for (File c : children) {
            if (c.isDirectory()) {
                collectJavaFiles(c, out);
            } else if (c.getName().endsWith(".java")) {
                out.add(c);
            }
        }
    }

    private static void assertUnderFrozen(long count, long frozen, String label) {
        if (count > frozen) {
            throw new AssertionError(label + ": count " + count + " exceeds frozen cap " + frozen
                    + ". Either remove the new call site(s) or, if this is an intentional"
                    + " ratchet reset, lower FROZEN_* only after confirming the count.");
        }
    }
}