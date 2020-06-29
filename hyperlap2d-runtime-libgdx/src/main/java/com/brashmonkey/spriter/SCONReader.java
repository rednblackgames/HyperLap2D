package com.brashmonkey.spriter;

import com.brashmonkey.spriter.Entity.CharacterMap;
import com.brashmonkey.spriter.Entity.ObjectInfo;
import com.brashmonkey.spriter.Entity.ObjectType;
import com.brashmonkey.spriter.Mainline.Key.BoneRef;
import com.brashmonkey.spriter.Mainline.Key.ObjectRef;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * This class parses a SCON file and creates a {@link Data} instance.
 * If you want to keep track of what is going on during the build process of the objects parsed from the SCON file,
 * you could extend this class and override the load*() methods for pre or post processing.
 * This could be e.g. useful for a loading screen which responds to the current building or parsing state.
 * @author mrdlink
 */
public class SCONReader {

	protected Data data;

	/**
	 * Creates a new SCON reader and will parse all objects in the given stream.
	 * @param stream the stream
	 */
	public SCONReader(InputStream stream){
		this.data = this.load(stream);
	}

	/**
	 * Creates a new SCON reader and will parse the given json string.
	 * @param json the json string
	 */
	public SCONReader(String json){
		this.data = this.load(json);
	}
	
	/**
	 * Parses the SCON object save in the given json string and returns the build data object.
	 * @param json the json string
	 * @return the built data
	 */
	protected Data load(String json){
		return load(JsonReader.parse(json));
	}
	
	/**
	 * Parses the SCON objects saved in the given stream and returns the built data object.
	 * @param stream the stream from the SCON file
	 * @return the built data
	 */
	protected Data load(InputStream stream){
		try {
			return load(JsonReader.parse(stream));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Reads the data from the given root element, i.e. the spriter_data node.
	 * @param root
	 * @return
	 */
	protected Data load(JSONObject root) {
		JSONArray folders = root.getJSONArray("folder");
		JSONArray entities = root.getJSONArray("entity");
		data = new Data(root.getString("scon_version"), root.getString("generator"), root.getString("generator_version"),
						Data.PixelMode.get(root.optInt("pixel_mode", 0)),
						folders.length(),	entities.length());
		loadFolders(folders);
		loadEntities(entities);
		return data;
	}

	/**
	 * Iterates through the given folders and adds them to the current {@link Data} object.
	 * @param folders a list of folders to load
	 */
	protected void loadFolders(JSONArray folders){
		for(int i = 0; i < folders.length(); i++){
			JSONObject repo = folders.getJSONObject(i);
			JSONArray files = repo.getJSONArray("file");

			Folder folder = new Folder(repo.getInt("id"), repo.optString("name", "no_name_"+i), files.length());
			loadFiles(files, folder);
			data.addFolder(folder);
		}
	}
	
	/**
	 * Iterates through the given files and adds them to the given {@link Folder} object.
	 * @param files a list of files to load
	 * @param folder the folder containing the files
	 */
	protected void loadFiles(JSONArray files, Folder folder){
		for(int j = 0; j < files.length(); j++){
			JSONObject f = files.getJSONObject(j);

			File file = new File(f.getInt("id"), f.getString("name"),
					new Dimension(f.optInt("width", 0), f.optInt("height", 0)),
					new Point(f.optFloat("pivot_x", 0f), f.optFloat("pivot_y", 0f)));
			
			folder.addFile(file);
		}
	}

	/**
	 * Iterates through the given entities and adds them to the current {@link Data} object.
	 * @param entities a list of entities to load
	 */
	protected void loadEntities(JSONArray entities){
		for(int i = 0; i < entities.length(); i++){
			JSONObject e = entities.getJSONObject(i);
			JSONArray infos = e.getJSONArray("obj_info");
			JSONArray charMaps = e.getJSONArray("character_map");
			JSONArray animations = e.getJSONArray("animation");
			Entity entity = new Entity(e.getInt("id"), e.getString("name"),
					animations.length(), charMaps.length(), infos.length());
			data.addEntity(entity);
			loadObjectInfos(infos, entity);
			loadCharacterMaps(charMaps, entity);
			loadAnimations(animations, entity);
		}
	}
	
	/**
	 * Iterates through the given object infos and adds them to the given {@link Entity} object.
	 * @param infos a list of infos to load
	 * @param entity the entity containing the infos
	 */
	protected void loadObjectInfos(JSONArray infos, Entity entity){
		for(int i = 0; i< infos.length(); i++){
			JSONObject info = infos.getJSONObject(i);
			ObjectInfo objInfo = new ObjectInfo(info.getString("name"),
									ObjectType.getObjectInfoFor(info.optString("type", "")),
									new Dimension(info.optFloat("w", 0f), info.optFloat("h", 0f)));
			entity.addInfo(objInfo);
			JSONObject frames = info.optJSONObject("frames");
			if(frames == null) continue;
			JSONArray frameIndices = frames.getJSONArray("i");
			for (int i1 = 0; i1 < frameIndices.length(); i1++) {
				JSONObject index = frameIndices.getJSONObject(i1);
				int folder = index.optInt("folder", 0);
				int file = index.optInt("file", 0);
				objInfo.frames.add(new FileReference(folder, file));
			}
		}
	}
	
	/**
	 * Iterates through the given character maps and adds them to the given {@link Entity} object.
	 * @param maps a list of character maps to load
	 * @param entity the entity containing the character maps
	 */
	protected void loadCharacterMaps(JSONArray maps, Entity entity){
		for(int i = 0; i< maps.length(); i++){
			JSONObject map = maps.getJSONObject(i);
			CharacterMap charMap = new CharacterMap(map.getInt("id"), map.optString("name", "charMap"+i));
			entity.addCharacterMap(charMap);
			JSONArray mappings = map.getJSONArray("map");
			for (int i1 = 0; i1 < mappings.length(); i1++) {
				JSONObject mapping = mappings.getJSONObject(i1);
				int folder = mapping.getInt("folder");
				int file = mapping.getInt("file");
				charMap.put(new FileReference(folder, file),
						new FileReference(mapping.optInt("target_folder", folder), mapping.optInt("target_file", file)));
			}
		}
	}
	
	/**
	 * Iterates through the given animations and adds them to the given {@link Entity} object.
	 * @param animations a list of animations to load
	 * @param entity the entity containing the animations maps
	 */
	protected void loadAnimations(JSONArray animations, Entity entity){
		for(int i = 0; i < animations.length(); i++){
			JSONObject a = animations.getJSONObject(i);
			JSONArray timelines = a.getJSONArray("timeline");
			JSONObject mainline = a.getJSONObject("mainline");
			JSONArray mainlineKeys = mainline.getJSONArray("key");
			Animation animation = new Animation(new Mainline(mainlineKeys.length()),
									  a.getInt("id"), a.getString("name"), a.getInt("length"),
									  a.optBoolean("looping", true),timelines.length());
			entity.addAnimation(animation);
			loadMainlineKeys(mainlineKeys, animation.mainline);
			loadTimelines(timelines, animation, entity);
			animation.prepare();
		}
	}
	
	/**
	 * Iterates through the given mainline keys and adds them to the given {@link Mainline} object.
	 * @param keys a list of mainline keys
	 * @param main the mainline
	 */
	protected void loadMainlineKeys(JSONArray keys, Mainline main){
		for(int i = 0; i < main.keys.length; i++){
			JSONObject k = keys.getJSONObject(i);
			JSONArray objectRefs = k.getJSONArray("object_ref");
			JSONArray boneRefs = k.getJSONArray("bone_ref");
			Curve curve = new Curve();
			curve.setType(Curve.getType(k.optString("curve_type","linear")));
			curve.constraints.set(k.optFloat("c1", 0f),k.optFloat("c2", 0f),k.optFloat("c3", 0f),k.optFloat("c4", 0f));
			Mainline.Key key = new Mainline.Key(k.getInt("id"), k.optInt("time", 0), curve,
					boneRefs.length(), objectRefs.length());
			main.addKey(key);
			loadRefs(objectRefs, boneRefs, key);
		}
	}
	
	/**
	 * Iterates through the given bone and object references and adds them to the given {@link Mainline.Key} object.
	 * @param objectRefs a list of object references
	 * @param boneRefs a list if bone references
	 * @param key the mainline key
	 */
	protected void loadRefs(JSONArray objectRefs, JSONArray boneRefs, Mainline.Key key){
		for (int i = 0; i < boneRefs.length(); i++) {
			JSONObject e = boneRefs.getJSONObject(i);
			BoneRef boneRef = new BoneRef(e.getInt("id"), e.getInt("timeline"),
					e.getInt("key"), key.getBoneRef(e.optInt("parent", -1)));
			key.addBoneRef(boneRef);
		}

		for (int i = 0; i < objectRefs.length(); i++) {
			JSONObject o = objectRefs.getJSONObject(i);
			ObjectRef objectRef = new ObjectRef(o.getInt("id"), o.getInt("timeline"),
					o.getInt("key"), key.getBoneRef(o.optInt("parent", -1)), o.optInt("z_index", 0));
			key.addObjectRef(objectRef);
		}
		Arrays.sort(key.objectRefs);
	}
	
	/**
	 * Iterates through the given timelines and adds them to the given {@link Animation} object.
	 * @param timelines a list of timelines
	 * @param animation the animation containing the timelines
	 * @param entity entity for assigning the timeline an object info
	 */
	protected void loadTimelines(JSONArray timelines, Animation animation, Entity entity){
		for(int i = 0; i< timelines.length(); i++){
			JSONObject t = timelines.getJSONObject(i);
			JSONArray keys = timelines.getJSONObject(i).getJSONArray("key");
			String name = t.getString("name");
			ObjectType type = ObjectType.getObjectInfoFor(t.optString("object_type", "sprite"));
			ObjectInfo info = entity.getInfo(name);
			if(info == null) info = new ObjectInfo(name, type, new Dimension(0,0));
			Timeline timeline = new Timeline(t.getInt("id"), name, info, keys.length());
			animation.addTimeline(timeline);
			loadTimelineKeys(keys, timeline);
		}
	}
	
	/**
	 * Iterates through the given timeline keys and adds them to the given {@link Timeline} object.
	 * @param keys a list if timeline keys
	 * @param timeline the timeline containing the keys
	 */
	protected void loadTimelineKeys(JSONArray keys, Timeline timeline){
		for(int i = 0; i< keys.length(); i++){
			JSONObject k = keys.getJSONObject(i);
			Curve curve = new Curve();
			curve.setType(Curve.getType(k.optString("curve_type", "linear")));
			curve.constraints.set(k.optFloat("c1", 0f),k.optFloat("c2", 0f),k.optFloat("c3", 0f),k.optFloat("c4", 0f));
			Timeline.Key key = new Timeline.Key(k.getInt("id"), k.optInt("time", 0), k.optInt("spin", 1), curve);
			JSONObject obj = k.optJSONObject("bone");
			if(obj == null) obj = k.getJSONObject("object");
			
			Point position = new Point(obj.optFloat("x", 0f), obj.optFloat("y", 0f));
			Point scale = new Point(obj.optFloat("scale_x", 1f), obj.optFloat("scale_y", 1f));
			Point pivot = new Point(obj.optFloat("pivot_x", 0f), obj.optFloat("pivot_y", (timeline.objectInfo.type == ObjectType.Bone)? .5f:1f));
			float angle = obj.optFloat("angle", 0f), alpha = 1f;
			int folder = -1, file = -1;
			if(obj.optString("name", "no_name_"+i).equals("object")){
				if(timeline.objectInfo.type == ObjectType.Sprite){
					alpha = obj.optFloat("a", 1f);
					folder = obj.optInt("folder", -1);
					file = obj.optInt("file", -1);
					File f = data.getFolder(folder).getFile(file);
					pivot = new Point(obj.optFloat("pivot_x", f.pivot.x), obj.optFloat("pivot_y", f.pivot.y));
					timeline.objectInfo.size.set(f.size);
				}
			}
			Timeline.Key.Object object;
			if(obj.optString("name", "no_name_"+i).equals("bone")) object = new Timeline.Key.Object(position, scale, pivot, angle, alpha, new FileReference(folder, file));
			else object = new Timeline.Key.Object(position, scale, pivot, angle, alpha, new FileReference(folder, file));
			key.setObject(object);
			timeline.addKey(key);
		}
	}
	
	/**
	 * Returns the loaded SCON data.
	 * @return the SCON data.
	 */
	public Data getData(){
		return data;
	}
	
}

