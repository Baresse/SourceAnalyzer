package lba.util.dependency;

import java.util.*;

/**
 * Class used to store package dependencies (detailed)
 */
public class DependencyManager {

    /**
     * Dependencies holder
     */
    private Map<String, SortedSet<String>> dependencies = new HashMap<String, SortedSet<String>>();

    /**
     * Add a dependency to a given package pkg
     *
     * @param pkg        initial package
     * @param dependency new dependency to the pkg package
     */
    public void addDependency(String pkg, String dependency) {

        SortedSet<String> deps;
        if (!dependencies.keySet().contains(pkg)) {
            deps = new TreeSet<String>();
            dependencies.put(pkg, deps);
        } else {
            deps = dependencies.get(pkg);
        }
        deps.add(dependency);
    }

    /**
     * Add a dependencies set to a given package pkg
     *
     * @param pkg          initial package
     * @param dependencies new dependencies to the pkg package
     */
    public void addDependencies(String pkg, SortedSet<String> dependencies) {

        SortedSet<String> deps;
        if (!this.dependencies.keySet().contains(pkg)) {
            deps = new TreeSet<String>();
            this.dependencies.put(pkg, deps);
        } else {
            deps = this.dependencies.get(pkg);
        }
        deps.addAll(dependencies);
    }

    /**
     * @param rootPackage
      * @return
     */
    public Map<String, SortedSet<String>> getNormalizedDependencies(String rootPackage) {

        // Normalized the dependencies with representative package name (only one depth after the rootPackage)
        Map<String, SortedSet<String>> depsNormalized = new HashMap<String, SortedSet<String>>();

        for (String pkg : dependencies.keySet()) {
            if (!pkg.equals(rootPackage)) {
                String pkgNormalized = getRepresentativePkgName(rootPackage, pkg);
                if (!depsNormalized.keySet().contains(pkgNormalized)) {
                    depsNormalized.put(pkgNormalized, new TreeSet<String>());
                }

                for (String dep : dependencies.get(pkg)) {
                    if (dep.startsWith(rootPackage)) {
                        Set<String> dependencies = depsNormalized.get(pkgNormalized);
                        String newDep = getRepresentativePkgName(rootPackage, dep);
                        // We don't want self-dependency
                        if (!newDep.equals(pkgNormalized)) {
                            dependencies.add(newDep);
                        }
                    }
                }
            }
        }
        return depsNormalized;
    }

    /**
     * This method will return the representative package name for Dot file generation
     * If rootPackage="fr.cnes.sitools" and dependency="fr.cnes.sitools.common.model.Resource" the method will return "common"
     * If the dependency does not share the same rootPackage, the method will return null
     *
     * @param rootPackage
     * @param dependency
     * @return
     */
    private String getRepresentativePkgName(String rootPackage, String dependency) {

        if (dependency == null || rootPackage == null || !dependency.startsWith(rootPackage)) {
            return null;
        }

        String value = dependency.substring(rootPackage.length() + 1);

        if (value.indexOf(".") > 0) {
            return value.substring(0, value.indexOf("."));
        } else {
            return value;
        }
    }

    public Map<String, SortedSet<String>> getDependencies() {
        return dependencies;
    }
}
