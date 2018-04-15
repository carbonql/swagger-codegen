package io.swagger.codegen.languages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import io.swagger.codegen.*;
import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenConstants;
import io.swagger.codegen.SupportingFile;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.FileProperty;
import io.swagger.models.properties.Property;

public class TypeScriptK8sClientCodegen extends AbstractTypeScriptClientCodegen {
    private static final Logger LOGGER = LoggerFactory.getLogger(TypeScriptK8sClientCodegen.class);
    private static final SimpleDateFormat SNAPSHOT_SUFFIX_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");

    public static final String NPM_NAME = "npmName";
    public static final String NPM_VERSION = "npmVersion";
    public static final String NPM_REPOSITORY = "npmRepository";
    public static final String SNAPSHOT = "snapshot";

    protected String npmName = null;
    protected String npmVersion = "1.0.0";
    protected String npmRepository = null;

    public TypeScriptK8sClientCodegen() {
        super();

        typeMapping.put("file", "Buffer");
        typeMapping.put("integer", "number");

        // clear import mapping (from default generator) as TS does not use it
        // at the moment
        importMapping.clear();

        outputFolder = "generated-code/typescript-k8s";
        embeddedTemplateDir = templateDir = "typescript-k8s";

        this.cliOptions.add(new CliOption(NPM_NAME, "The name under which you want to publish generated npm package"));
        this.cliOptions.add(new CliOption(NPM_VERSION, "The version of your npm package"));
        this.cliOptions.add(new CliOption(NPM_REPOSITORY, "Use this property to set an url your private npmRepo in the package.json"));
        this.cliOptions.add(new CliOption(SNAPSHOT, "When setting this property to true the version will be suffixed with -SNAPSHOT.yyyyMMddHHmm", BooleanProperty.TYPE).defaultValue(Boolean.FALSE.toString()));
    }

    @Override
    public void processOpts() {
        super.processOpts();
        supportingFiles.add(new SupportingFile("api.mustache", null, "api.ts"));
        supportingFiles.add(new SupportingFile("git_push.sh.mustache", "", "git_push.sh"));
        supportingFiles.add(new SupportingFile("gitignore", "", ".gitignore"));

        if(additionalProperties.containsKey(NPM_NAME)) {
            addNpmPackageGeneration();
        }
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property){
        super.postProcessModelProperty(model, property);

        if (
            property.complexType != null &&
            property.complexType.equals("IoK8sApimachineryPkgUtilIntstrIntOrString")
        ) {
            property.complexType = null;
            property.datatype = "string | number";
            property.datatypeWithEnum = "string | number";
            property.baseType = "string | number";
            property.isPrimitiveType = true;
            property.isString = true;
            property.isNumber = true;
            property.hasMore = true;
            property.hasMoreNonReadOnly = true;
            property.isContainer = false;
            property.isNotContainer = true;
        }

        if (
            property.complexType != null &&
            (property.complexType.equals("IoK8sApimachineryPkgApisMetaV1Time") ||
            property.complexType.equals("IoK8sApimachineryPkgApisMetaV1MicroTime"))
        ) {
            property.complexType = null;
            property.datatype = "string";
            property.datatypeWithEnum = "string";
            property.baseType = "string";
            property.isPrimitiveType = true;
            property.isString = true;
            property.isNumber = true;
            property.hasMore = true;
            property.hasMoreNonReadOnly = true;
            property.isContainer = false;
            property.isNotContainer = true;
        }
    }

    private void addNpmPackageGeneration() {
        if(additionalProperties.containsKey(NPM_NAME)) {
            this.setNpmName(additionalProperties.get(NPM_NAME).toString());
        }

        if (additionalProperties.containsKey(NPM_VERSION)) {
            this.setNpmVersion(additionalProperties.get(NPM_VERSION).toString());
        }

        if (additionalProperties.containsKey(SNAPSHOT) && Boolean.valueOf(additionalProperties.get(SNAPSHOT).toString())) {
            this.setNpmVersion(npmVersion + "-SNAPSHOT." + SNAPSHOT_SUFFIX_FORMAT.format(new Date()));
        }
        additionalProperties.put(NPM_VERSION, npmVersion);

        if (additionalProperties.containsKey(NPM_REPOSITORY)) {
            this.setNpmRepository(additionalProperties.get(NPM_REPOSITORY).toString());
        }

        //Files for building our lib
        supportingFiles.add(new SupportingFile("package.mustache", getPackageRootDirectory(), "package.json"));
        supportingFiles.add(new SupportingFile("tsconfig.mustache", getPackageRootDirectory(), "tsconfig.json"));
    }

    private String getPackageRootDirectory() {
        String indexPackage = modelPackage.substring(0, Math.max(0, modelPackage.lastIndexOf('.')));
        return indexPackage.replace('.', File.separatorChar);
    }

    @Override
    public String getName() {
        return "typescript-k8s";
    }

    @Override
    public String getHelp() {
        return "Generates a TypeScript Kubernetes client library.";
    }

    @Override
    public boolean isDataTypeFile(final String dataType) {
        return dataType != null && dataType.equals("Buffer");
    }

    @Override
    public String getTypeDeclaration(Property p) {
        if (p instanceof FileProperty) {
            return "Buffer";
        }
        return super.getTypeDeclaration(p);
    }


    public void setNpmName(String npmName) {
        this.npmName = npmName;
    }

    public void setNpmVersion(String npmVersion) {
        this.npmVersion = npmVersion;
    }

    public String getNpmVersion() {
        return npmVersion;
    }

    public String getNpmRepository() {
        return npmRepository;
    }

    public void setNpmRepository(String npmRepository) {
        this.npmRepository = npmRepository;
    }
}
