package bwfdm.replaydh.workflow.export.dataverse;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.eclipse.jgit.util.FileUtils;
import org.swordapp.client.AuthCredentials;
import org.swordapp.client.DepositReceipt;
import org.swordapp.client.SWORDClientException;
import bwfdm.replaydh.workflow.export.generic.SwordExporter;

/**
 * 
 * @author Florian Fritze, Volodymyr Kushnarenko
 *
 */
public abstract class DataverseRepository extends SwordExporter {
	
	public DataverseRepository(AuthCredentials authCredentials) {
		super(authCredentials);
	}

	
	/**
	 * Get the Atom Feed of a Dataverse collection URL
	 * @param dataverseURL
	 * @param auth
	 * @return
	 * @throws SWORDClientException
	 * @throws MalformedURLException
	 */
	public abstract Feed getAtomFeed(String dataverseURL) throws SWORDClientException, MalformedURLException;
	
	
	/**
	 * Replaces a metadata entry
	 * @param doiUrl
	 * @param zipFile
	 * @param metadataFileXML
	 * @param metadataMap
	 * @return
	 * @throws IOException
	 * @throws SWORDClientException
	 */
	public abstract boolean replaceMetadata(String doiUrl, File zipFile, File metadataFileXML, Map<String, List<String>> metadataMap) throws IOException, SWORDClientException;
	
	/**
	 * Replace metadata entry and add file
	 * @param collectionURL TODO
	 * @param doiUrl
	 * @param zipFile
	 * @param metadataFileXML
	 * @param metadataMap
	 * @return
	 * @throws IOException
	 * @throws SWORDClientException
	 */
	public abstract boolean replaceMetadataAndAddFile(String collectionURL, String doiUrl, File zipFile, File metadataFileXML, Map<String, List<String>> metadataMap) throws IOException, SWORDClientException;

	/**
	 * Get the entry in the Atom Feed which refers to the URL of the metadata entry in Dataverse. This entry is necessary to add files to the metadata entry in Dataverse.
	 * @param feed
	 * @return
	 */
	public abstract Entry getUserAvailableMetadataset(Feed feed, String doiId);
	
	/**
	 * Returns all the entries of a DataVerse URL saved in ID --> Title map
	 * @param feed
	 * @return
	 */
	public abstract Map<String, String> getMetadataSetsWithId(Feed feed);
	
	/**
	 * Returns a Map of all files in a dataverse collection
	 * @param chosenCollection
	 * @return
	 * @throws SWORDClientException 
	 * @throws MalformedURLException 
	 */
	public abstract Map<String, String> getDatasetsInDataverseCollection(String chosenCollection) throws MalformedURLException, SWORDClientException;

	/**
	 * Copied from class SWORDClient but modified for the Dataverse REST API
	 * @param doiUrl
	 * @param apiKey
	 * @return TODO
	 * @throws SWORDClientException 
	 * @throws IOException 
	 */
	public abstract String getJSONMetadata(String doiUrl) throws SWORDClientException, IOException;
	
	
	/**
	 * @param collectionURL holds the collection URL where the metadata will be exported to
	 * @param metadataMap holds the metadata itself
	 * @return
	 */
	public String exportMetadata(String collectionURL, Map<String, List<String>> metadataMap) {
		DepositReceipt returnValue = null;
		requireNonNull(metadataMap);
		requireNonNull(collectionURL);
		returnValue = (DepositReceipt) exportElement(collectionURL, SwordRequestType.DEPOSIT, MIME_FORMAT_ATOM_XML, null, null, metadataMap);
		if(returnValue != null) {
			return returnValue.getEntry().getEditMediaLinkResolvedHref().toString();
		} else {
			log.error("No return value from publishElement method");
			return null;
		}
	}

	/**
	 * @param collectionURL holds the collection URL where items will be exported to
	 * @param packedFiles holds a zip file which can contain one or multiple files
	 * @param metadataMap holds the metadata which is necessary for the ingest
	 * @return
	 * @throws SWORDClientException 
	 * @throws IOException 
	 */
	public boolean exportMetadataAndFile(String collectionURL, File zipFile, Map<String, List<String>> metadataMap) throws IOException, SWORDClientException {
		Entry entry = null;
		String doiId = this.exportMetadata(collectionURL,  metadataMap);
		int beginDOI=doiId.indexOf("doi:");
		int end=doiId.length();
		entry=getUserAvailableMetadataset(getAtomFeed(collectionURL),doiId.substring(beginDOI, end));
		return this.exportFile(entry.getEditMediaLinkResolvedHref().toString(), zipFile);
	}
	
	/**
	 * @param metadataSetURL The URL where to export the zipFile to.
	 * @param zipFile A zip file that should be exported.
	 * @return
	 * @throws IOException
	 */
	public boolean exportFile(String metadataSetHrefURL, File zipFile) throws IOException {
		String packageFormat = getPackageFormat(zipFile.getName()); //zip-archive or separate file
		DepositReceipt returnValue = (DepositReceipt) exportElement(metadataSetHrefURL, SwordRequestType.DEPOSIT, MIME_FORMAT_ZIP, packageFormat, zipFile, null);
		FileUtils.delete(zipFile);
		if (returnValue != null) {
			return true;
		} else {
			return false;
		}
	}
}
