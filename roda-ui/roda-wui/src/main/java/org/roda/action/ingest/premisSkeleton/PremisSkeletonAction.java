package org.roda.action.ingest.premisSkeleton;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.roda.action.orchestrate.Plugin;
import org.roda.action.orchestrate.PluginException;
import org.roda.common.PremisUtils;
import org.roda.core.common.InvalidParameterException;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.v2.Representation;
import org.roda.core.data.v2.RepresentationFilePreservationObject;
import org.roda.core.data.v2.RepresentationPreservationObject;
import org.roda.core.metadata.v2.premis.PremisFileObjectHelper;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.core.metadata.v2.premis.PremisRepresentationObjectHelper;
import org.roda.index.IndexService;
import org.roda.model.AIP;
import org.roda.model.File;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.storage.Binary;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;
import org.roda.storage.fs.FSUtils;

public class PremisSkeletonAction implements Plugin<AIP> {
  private final Logger logger = Logger.getLogger(getClass());

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Premis skeleton action";
  }

  @Override
  public String getDescription() {
    return "Create the premis related files with the basic information";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    return new ArrayList<>();
  }

  @Override
  public Map<String, String> getParameterValues() {
    return new HashMap<>();
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    // no params
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    try {
      Path temp = Files.createTempDirectory("temp");
      for (AIP aip : list) {
        try {
          for (String representationID : aip.getRepresentationIds()) {
            RepresentationPreservationObject pObject = new RepresentationPreservationObject();
            pObject.setAipId(aip.getId());
            pObject.setId(representationID);
            pObject.setPreservationLevel("");
            Representation representation = model.retrieveRepresentation(aip.getId(), representationID);
            List<RepresentationFilePreservationObject> pObjectPartFiles = new ArrayList<RepresentationFilePreservationObject>();
            for (String fileID : representation.getFileIds()) {
              File file = model.retrieveFile(aip.getId(), representationID, fileID);
              Binary binary = storage.getBinary(file.getStoragePath());
              Path pathFile = Paths.get(temp.toString(), file.getStoragePath().getName());
              Files.copy(binary.getContent().createInputStream(), pathFile, StandardCopyOption.REPLACE_EXISTING);
              RepresentationFilePreservationObject premisObject = PremisUtils.createPremisFromFile(file, binary,
                "PremisSkeletonAction");
              Path premis = Files.createTempFile(file.getId(), ".premis.xml");
              PremisFileObjectHelper helper = new PremisFileObjectHelper(premisObject);
              helper.saveToFile(premis.toFile());
              model.createPreservationMetadata(aip.getId(), representationID, file.getId() + ".premis.xml",
                (Binary) FSUtils.convertPathToResource(premis.getParent(), premis));
              if (pObject.getRootFile() == null) {
                pObject.setRootFile(premisObject);
              } else {
                pObjectPartFiles.add(premisObject);
              }
            }
            pObject.setPartFiles(
              pObjectPartFiles.toArray(new RepresentationFilePreservationObject[pObjectPartFiles.size()]));
            Path premisRepresentation = Files.createTempFile("representation", ".premis.xml");
            PremisRepresentationObjectHelper helper = new PremisRepresentationObjectHelper(pObject);
            helper.saveToFile(premisRepresentation.toFile());
            model.createPreservationMetadata(aip.getId(), representationID,"representation.premis.xml",
              (Binary) FSUtils.convertPathToResource(premisRepresentation.getParent(), premisRepresentation));
          }
        } catch (ModelServiceException mse) {
          logger.error("Error processing AIP " + aip.getId() + ": " + mse.getMessage(), mse);
        } catch (StorageServiceException sse) {
          logger.error("Error processing AIP " + aip.getId() + ": " + sse.getMessage(), sse);
        } catch (PremisMetadataException pme) {
          logger.error("Error processing AIP " + aip.getId() + ": " + pme.getMessage(), pme);
        }
      }
    } catch (IOException ioe) {
      logger.error("Error executing FastCharacterizationAction: " + ioe.getMessage(), ioe);
    }
    return null;
  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

}
