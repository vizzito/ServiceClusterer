package org.clusterer.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import weka.classifiers.lazy.IBk;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;
import java.util.List;
import java.util.ArrayList;


/**
 *
 * @author rp
 */
public class ClasificadorSPAM {
    // Constantes
    public static String ETQ_SPAM = "spam";
    public static String ETQ_NO_SPAM = "no-spam";
    
    // Parámetros
    private int numVecinos;
    
    // Variables del objeto
    private IBk clasificador;
    private StringToWordVector filtro;
    private Instances datasetInicial;
    private Instances datasetFiltrado;

    // Constructor
    public ClasificadorSPAM(int numVecinos) {
        this.numVecinos = numVecinos;
    }

    // Metodos privados
    
    /**
     * Crea e inicializa el clasificador k-nn (num. vecinos + ponderacion distancia)
     */
    public void inicializar() {
        this.clasificador = new IBk();
        this.clasificador.setKNN(this.numVecinos);
        this.clasificador.setDistanceWeighting(new SelectedTag(IBk.WEIGHT_INVERSE, IBk.TAGS_WEIGHTING));
    }
    
    /**
     * (1) Crea un dataset con una instancia con 2 atributos (TEXTO+CLASE) para cada fichero del directorio indicado.
     * (2) Filtra ese dataset con un StringToWordVector para transfomra el atributos TEXTO en un conjunto 
     *    de atributos numericos (1 valor por cada palabra/token seleccionada) 
     * (3) Con el dataset resultado de ese filtrado se entrena el  clasificador
     * (4) Las instancia de los datasets temporal no se necesitan, pero se debe mantener su cabecera (vector de Attribute) 
     * @param pathDirectorio
     * @throws java.lang.Exception
     */
    public void entrenarDirectorio(String pathDirectorio) throws Exception {
        // Crear y cargar contenido textual de los ficheros
        datasetInicial = crearDatasetInicial();
        File directorio = new File(pathDirectorio);
        cargarFicherosEntrenamiento(directorio);

        System.out.println("\t... datos de entrenamiento cargados");
        
        // Filtrar (en bloque) dataset inicial -> covertir texto en vector numerico
        filtro = crearFiltro(datasetInicial);
        datasetFiltrado = Filter.useFilter(datasetInicial, filtro);
     //   datasetFiltrado.setClass(datasetFiltrado.attribute("CLASE")); // Otra forma equivalente: datasetFiltrado.setClassIndex(0);

        System.out.println("\t... datos de entrenamiento filtrados");
                
        // Entrenar clasificador
        clasificador.buildClassifier(datasetFiltrado);

        System.out.println("\t... clasificador entrenado");
        
        
        // Para depuración -> guardar en disco los ARFF
        volcarDatsets();
       
        
        // Liberar las instancias (ya no son necesarias [IBk hace su propia copia])
        // Bastará con mantener las cabeceras de los datasets (Attribute) [usadas en evaluarDirectorio()]
        datasetInicial.delete();
        datasetFiltrado.delete();
    }

    /**
     * Recorre los ficheros del directorio indicado, los convierte en instancias WEKA y los clasifica
     * (1) Crea una instancia inicial TEXTO+CLASE 
     * (2) La filtra con el StringtoWordVector creado con el conjunto de entrenamiento
     * (3) Clasifica la instancia filtrado y obtiene el indice de la clase predicha
     * @param pathDirectorio
     * @throws java.lang.Exception
     */
    public void evaluarDirectorio(String pathDirectorio) throws Exception {
       Instance instanciaInicial;
       Instance instanciaFiltrada;
       int idxClasePredicha;
       String clasePredicha;
       
       System.out.println("CLASIFICACION DEL DIRECTORIO ["+pathDirectorio+"]");
       File directorio = new File(pathDirectorio);
       File[] listaFicheros = directorio.listFiles();
       for (File fichero :listaFicheros) {
            instanciaInicial = crearInstanciaInicial(fichero);
            
            // Filtrar instancia a clasificar (una a una) -> covertir texto en vector numerico
            filtro.input(instanciaInicial);
            instanciaFiltrada = filtro.output();
            
            // Clasificar la instancia filtrada
            idxClasePredicha = (int) clasificador.classifyInstance(instanciaFiltrada);
            
            // Mostrar valor predicho
            clasePredicha = datasetFiltrado.attribute("CLASE").value(idxClasePredicha);
            System.out.println("FICHERO ["+fichero.getName()+"] -> CLASE: "+clasePredicha);
       }        
    }

    /**
     * Crea un dataset (inicialmente vacio) con 2 atributos para almacenar los datos de entrenamiento.
     * - Atributo CLASE: atributo nominal con 2 valores: {spam, no-spam}.          
     * - Atributo TEXTO: atributo de tipo String. 
     * Estable CLASE como atributo clase
     * @return
     */
    private Instances crearDatasetInicial() {
        // Crear lista de clases
   //     List<String> clases = new ArrayList<String>();
      //  clases.add(ETQ_SPAM);
    //   clases.add(ETQ_NO_SPAM);

        // Crear lista de atributos (cabecera del dataset)
        ArrayList<Attribute> atributos = new ArrayList<Attribute>();
     //   atributos.add(new Attribute("CLASE", clases));   // Atributo nominal
        atributos.add(new Attribute("TEXTO", (List<String>) null));  // Atributo String

        Instances dataset = new Instances("original", atributos, 10000000);
    //.setClass(dataset.attribute("CLASE")); // Otra forma equivalente: dataset.setClassIndex(0);

        return (dataset);
    }

    
    /**
     * Crea y configura un filtro StringToWordVector 
     * @param dataset
     * @return
     * @throws java.lang.Exception
     */
    private StringToWordVector crearFiltro(Instances dataset) throws Exception {
        StringToWordVector filtroTemp = new StringToWordVector();
        // Establecer el formato del dataset de entrada
        filtroTemp.setInputFormat(dataset);
        // Indica que atributos procesar
     //   int[] idxAtributosFiltrar = {0};
      //  filtroTemp.setAttributeIndicesArray(idxAtributosFiltrar); // Otra forma equivalente: filtroTemp.setSelectedRange("2");        
        return (filtroTemp);
    }
    
    /**
     * Recorre el directorio con los ficheros de entrenamiento creando una instancia (CLASE, TEXTO) para cada uno
     * @param directorio
     */
    private void cargarFicherosEntrenamiento(File directorio) {
        Instance instancia;
  
        File[] listaFicheros = directorio.listFiles();
        for (File fichero : listaFicheros) {
            instancia = crearInstanciaInicial(fichero);
            datasetInicial.add(instancia);
        }
    }

    /**
     * Crea una instancia (CLASE, TEXTO) para el fichero indicado
     * @param pathFichero
     * @return
     */
    private Instance crearInstanciaInicial(File fichero) {
        String texto = extraerTexto(fichero);
     //   String clase = extraerClase(fichero.getName());

        // Instance instancia = new Instance(datasetInicial.numAttributes());  // A partir de version 3.7 Instance es una clase abstracta
        Instance instancia = new  DenseInstance(datasetInicial.numAttributes());
        instancia.setDataset(datasetInicial);
        instancia.setValue(datasetInicial.attribute("TEXTO"), texto);
       // instancia.setClassValue(clase);
        return(instancia);
    }
 
    /**
     * Identifica la clase de un fichero: los ficheros SPAM tiene el nombre "spmsgXXXX.txt"
     * @param pathFichero
     * @return
     */
    private String extraerClase(String nombreFichero) {
        if (nombreFichero.startsWith("spmsg")) {
            return (ETQ_SPAM);
        } else {
            return (ETQ_NO_SPAM);
        }
    }

    /**
     * Extrae linea a linea el texto del fichero
     * @param pathFichero
     * @return
     */
    private String extraerTexto(File fichero) {
        StringBuffer buffer = new StringBuffer();

        try {
            BufferedReader in = new BufferedReader(new FileReader(fichero));
            String linea = in.readLine();
            while (linea != null) {
                buffer.append(linea + " ");
                linea = in.readLine();
            }
            in.close();
        } catch (Exception e) {
            System.err.println("Error en lectura de fichero [" + fichero + "] \n" + e.getMessage());
            System.exit(0);
        }

        return (buffer.toString());
    }

    /**
     * Escribe los datasets en archivos ARFF
     * @throws java.io.IOException
     */
    private void volcarDatsets() throws IOException {        
        ArffSaver guardarARFF = new ArffSaver();        
        
        guardarARFF.setInstances(datasetInicial);
        guardarARFF.setDestination(new FileOutputStream("inicial.arff"));
        guardarARFF.writeBatch();

        guardarARFF.setInstances(datasetFiltrado);
        guardarARFF.setDestination(new FileOutputStream("filtrado.arff"));
        guardarARFF.writeBatch();
        
        System.out.println("\t[los datasets creados se han volcado en inicial.arff, filtrado.arff]");
    }
    
    
    // Metodos estáticos [main()]
    public static void main(String[] args) {
//        if (args.length != 3) {
//            mensajeError();
//            System.exit(0);
//        }
        try {
            ClasificadorSPAM miClasificador = new ClasificadorSPAM(Integer.parseInt("1"));
            System.out.println("Crear clasificador ... ");
            miClasificador.inicializar();
            System.out.println("Entrenar clasificador ... ");
            miClasificador.entrenarDirectorio("/home/panther/botomUp1");
           // System.out.println("Clasificar ficheros ... ");
          //  miClasificador.evaluarDirectorio(args[2]);
        } catch (Exception e) {
            System.err.println("Error en clasificación: " + e.getMessage());
        }
    }

    private static void mensajeError() {
        System.out.println("ERROR: parametros incorrectos");
        System.out.println("formato: java ClasificadorSPAM [num. vecinos] [dir. entrenamiento] [dir. evaluacion]");
    }

    
    
}  // Fin clase ClasificadorSPAM