package org.roda.action.ingest.fastCharacterization;

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
import org.roda.action.ingest.fastCharacterization.utils.DroidException;
import org.roda.action.ingest.fastCharacterization.utils.DroidUtils;
import org.roda.action.orchestrate.Plugin;
import org.roda.action.orchestrate.PluginException;
import org.roda.common.PremisUtils;
import org.roda.core.common.InvalidParameterException;
import org.roda.core.data.FileFormat;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.RepresentationFile;
import org.roda.core.data.v2.Fixity;
import org.roda.core.data.v2.Representation;
import org.roda.core.data.v2.RepresentationFilePreservationObject;
import org.roda.core.metadata.v2.premis.PremisFileObjectHelper;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.index.IndexService;
import org.roda.model.AIP;
import org.roda.model.File;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.model.utils.ModelUtils;
import org.roda.storage.Binary;
import org.roda.storage.DefaultStoragePath;
import org.roda.storage.StoragePath;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;
import org.roda.storage.fs.FSUtils;
import org.roda.storage.fs.FileStorageService;


public class FastCharacterizationAction implements Plugin<AIP> {
  private final Logger logger = Logger.getLogger(getClass());
  Path signaturePath;
  
  @Override
  public void init() throws PluginException {
    signaturePath = Paths.get("/home/sleroux/roda/config/DROID_SignatureFile_V82.xml");
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Fast characterization action";
  }

  @Override
  public String getDescription() {
    return "Create the premis files with the basic information (format and puid)";
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
    try{
      DroidUtils droidUtils = DroidUtils.getInstance(signaturePath);
      Path temp = Files.createTempDirectory("temp");
        for(AIP aip : list){
          logger.debug("Creating basic premis for AIP "+aip.getId());
          try{
            for(String representationID : aip.getRepresentationIds()){
              Representation representation = model.retrieveRepresentation(aip.getId(), representationID);
              for(String fileID : representation.getFileIds()){
                String fileName = fileID+".premis.xml";
                File file = model.retrieveFile(aip.getId(), representationID, fileID);
                Binary binary = storage.getBinary(file.getStoragePath());
                Path pathFile = Paths.get(temp.toString(), file.getStoragePath().getName());
                Files.copy(binary.getContent().createInputStream(), pathFile, StandardCopyOption.REPLACE_EXISTING);
                FileFormat format = droidUtils.execute(pathFile);
                
                RepresentationFilePreservationObject premisObject = PremisUtils.getPremisFile(storage,aip.getId(),representationID,fileName);
                premisObject = PremisUtils.addFormatToPremis(premisObject, format);
                
                Path premis = Files.createTempFile(file.getId(), ".premis.xml");
                PremisFileObjectHelper helper = new PremisFileObjectHelper(premisObject);
                helper.saveToFile(premis.toFile());
                model.createPreservationMetadata(aip.getId(), representationID, fileName, (Binary)FSUtils.convertPathToResource(premis.getParent(), premis));
              }
            }
          }catch(ModelServiceException mse){
            logger.error("Error processing AIP "+aip.getId()+": "+mse.getMessage(),mse);
          } catch (StorageServiceException sse) {
            logger.error("Error processing AIP "+aip.getId()+": "+sse.getMessage(),sse);
          } catch (DroidException de) {
            logger.error("Error processing AIP "+aip.getId()+": "+de.getMessage(),de);
          } catch (PremisMetadataException pme) {
            logger.error("Error processing AIP "+aip.getId()+": "+pme.getMessage(),pme);
          }
        }
    }catch(IOException ioe){
      logger.error("Error executing FastCharacterizationAction: "+ioe.getMessage(),ioe);
    } catch (DroidException de) {
      logger.error("Error executing FastCharacterizationAction: "+de.getMessage(),de);
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
