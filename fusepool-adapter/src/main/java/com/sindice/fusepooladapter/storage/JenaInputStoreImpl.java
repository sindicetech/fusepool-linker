/*
 * Created by Sindice LTD http://sindicetech.com
 * Sindice LTD licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sindice.fusepooladapter.storage;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.jena.commons.Tria2JenaUtil;
import org.apache.jena.riot.RiotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
/**
 *
 * implementation of {@link com.sindice.fusepooladapter.storage.InputTripleStore } that uses Jena rdf framework
 *
 *
 */
public class JenaInputStoreImpl implements InputTripleStore {
  private static final Logger logger = LoggerFactory.getLogger(JenaInputStoreImpl.class);
  private  final String datafolder;
  private final Tria2JenaUtil t2j;
  
  public JenaInputStoreImpl(String datafolder){
    Path dataPath = Paths.get(datafolder);
    if (Files.exists(dataPath)){
      if (! Files.isDirectory(dataPath)){
        throw new IllegalArgumentException("file instead of folder specified for output data");
      }
    }
    this.datafolder = datafolder;
    this.t2j = new Tria2JenaUtil(new HashMap<BNode, Node>());
  }
  /**
   * cleans and populate the triplestore by triples from input collection 
   */
  @Override
  public int populate(TripleCollection triples) {
    Path dataPath = Paths.get(datafolder);
	  try {
		  if (Files.exists(dataPath)) {
			  DirectoryStream<Path> files;

			  files = Files.newDirectoryStream(dataPath);

			  if (files != null) {

				  for (Path filePath : files) {

					  try {
						  Files.delete(filePath);
					  } catch (IOException ex) {
						  for (int i = 0; i < 10; i++) {
							  try {
								  System.gc();
								  Files.delete(filePath);
							  } catch (IOException ex1) {
								  try {
									  Thread.sleep(10);
								  } catch (InterruptedException ex2) {
									  Thread.currentThread().interrupt();
								  }
								  continue;
							  }
							  break;
						  }
					  }
				  }
			  }
		  }
    } catch (IOException e) {
      throw new RuntimeException("error cleaning the store ", e);
    }
    Dataset dataset = TDBFactory.createDataset(datafolder);
    dataset.begin(ReadWrite.WRITE);
    // Get model inside the transaction
    Model model = dataset.getDefaultModel();
    model.removeAll();
    Iterator<Triple> iterator = triples.iterator();
    while (iterator.hasNext()) {
      Triple triple = iterator.next();
      com.hp.hpl.jena.graph.Triple jenaTriple = t2j.convertTriple(triple);
        try {
          model.getGraph().add(jenaTriple);
        } catch (RiotException e1){
          logger.warn("skipping {}", triple.toString());
        }
    }
    int size = (int) model.size();
    dataset.commit();
    TDB.sync(dataset);
    dataset.end();
    return size;
  }

  public void init() {
   //nothing to do in this class

  }

}
