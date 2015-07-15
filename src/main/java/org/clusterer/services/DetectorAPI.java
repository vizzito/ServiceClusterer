package org.clusterer.services;



/**
 * 
 */
//@Controller
//@EnableAutoConfiguration
//@RequestMapping("/detector")
public class DetectorAPI
{
	//
	//	@Resource
	//	ServicesAPI treeGeneratorService;
	//
	//	@RequestMapping(value = "check", method = RequestMethod.GET)
	//	protected void checkService(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
	//			IOException
	//	{
	//		final PrintWriter out = response.getWriter();
	//		out.println("Service Running!");
	//	}
	//
	//	@RequestMapping(value = "ap-detector", method = RequestMethod.POST)
	//	protected void detectorService(final HttpServletRequest request, final HttpServletResponse response) throws IOException
	//	{
	//		final AntipatternDetector s = new AntipatternDetector();
	//		final ArrayList<HashMap<String, Object>> antiPatterns = new ArrayList<HashMap<String, Object>>();
	//		try
	//		{
	//			//			for (final String url : treeGeneratorService.getListFileNames())
	//			//			{
	//			//				s.setWsdlUrl(new URL("file:/tmp/" + url));
	//			//				final HashMap<String, Object> map = new HashMap<>();
	//			//				final Antipattern[] result = s.analyze();
	//			//				map.put("fileName", url);
	//			//				map.put("antiPatterns", result);
	//			//				antiPatterns.add(map);
	//			//			}
	//
	//			for (final MultipartFile file : treeGeneratorService.getListFiles())
	//			{
	//				//s.setWsdlUrl(new URL("file:/tmp/" + url));
	//				s.setFile(file);
	//				final HashMap<String, Object> map = new HashMap<>();
	//				final Antipattern[] result = s.analyze();
	//				map.put("fileName", file.getOriginalFilename());
	//				map.put("antiPatterns", result);
	//				antiPatterns.add(map);
	//			}
	//
	//		}
	//		catch (final Exception e)
	//		{
	//			e.printStackTrace();
	//		}
	//		final Gson g = new Gson();
	//		final String jsonResult = g.toJson(antiPatterns);
	//		final PrintWriter out = response.getWriter();
	//		out.println(jsonResult);
	//	}
	//
	//	private Description getDescriptionFromWSDLFile(final MultipartFile wsdlFile)
	//	{
	//		WSDLReader reader = null;
	//		try
	//		{
	//			reader = WSDLFactory.newInstance().newWSDLReader();
	//		}
	//		catch (final WSDLException e)
	//		{
	//			e.printStackTrace();
	//		}
	//		try
	//		{
	//			final Description description = reader.read(new InputSource(wsdlFile.getInputStream()));
	//			return description;
	//		}
	//		catch (WSDLException | URISyntaxException | IOException e)
	//		{
	//			e.printStackTrace();
	//		}
	//		return null;
	//	}

}
