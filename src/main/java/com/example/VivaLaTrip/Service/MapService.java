package com.example.VivaLaTrip.Service;

import com.example.VivaLaTrip.Entity.Documents;
import com.example.VivaLaTrip.Entity.KakaoGeoRes;
import com.example.VivaLaTrip.Entity.Places;
import com.example.VivaLaTrip.Repository.MapRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class MapService {

    private final MapRepository mapRepository;

    public MapService(MapRepository mapRepository) {
        this.mapRepository = mapRepository;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ResponseBody
    public List<Documents> MapParsing(String word) throws UnsupportedEncodingException, UnirestException, JsonProcessingException {

//        log.info("서비스  word값 : "+word);

        String APIKey = "KakaoAK ";

        HashMap<String, Object> map1 = new HashMap<>();
        HashMap<String, Object> map2 = new HashMap<>();
        String apiURL ="https://dapi.kakao.com/v2/local/search/keyword.json?page=1&query="
                + URLEncoder.encode(word, StandardCharsets.UTF_8);

        HttpResponse<JsonNode>[] response= new HttpResponse[3];

        response[0] = Unirest.get(apiURL)
                .header("Authorization", APIKey)
                .asJson();

        apiURL ="https://dapi.kakao.com/v2/local/search/keyword.json?page=2&query="
                + URLEncoder.encode(word, "UTF-8");

        response[1] = Unirest.get(apiURL)
                .header("Authorization", APIKey)
                .asJson();

        apiURL ="https://dapi.kakao.com/v2/local/search/keyword.json?page=3&query="
                + URLEncoder.encode(word, "UTF-8");

        response[2] = Unirest.get(apiURL)
                .header("Authorization", APIKey)
                .asJson();

        ObjectMapper objectMapper =new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);


        KakaoGeoRes bodyJson[] = new KakaoGeoRes[3];

        bodyJson[0] = objectMapper.readValue(response[0].getBody().toString(), KakaoGeoRes.class);
        bodyJson[1] = objectMapper.readValue(response[1].getBody().toString(), KakaoGeoRes.class);
        bodyJson[2] = objectMapper.readValue(response[2].getBody().toString(), KakaoGeoRes.class);

        List<Documents> result = new ArrayList<>();

        result.addAll(bodyJson[0].getDocuments());
        result.addAll(bodyJson[1].getDocuments());
        result.addAll(bodyJson[2].getDocuments());

        return result;
    }

    public List<Places> MapParsingDB(String word) {

        List<Places> bodyJson = mapRepository.findByAddressNameContains(word, Sort.by(Sort.Order.desc("popularity")));

//        log.info("데베에서 뽑아온 Places값 :"+bodyJson.toString());


        if (bodyJson.size() > 100) {
            return bodyJson.subList(0, 100);
        }
        return bodyJson;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ResponseBody
    public List<Documents> HotelPasrsingTest() throws UnirestException, JsonProcessingException {

        String APIKey = "KakaoAK 00996a550c5152989e6ad63d03958d4f";
        String apiURL ="https://dapi.kakao.com/v2/local/search/category.json?page=1&category_group_code=AD5&x=126.97679&y=37.57740&radius=20000";

        HttpResponse<JsonNode> response = Unirest.get(apiURL)
                .header("Authorization", APIKey)
                .asJson();


        ObjectMapper objectMapper =new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        KakaoGeoRes bodyJson = objectMapper.readValue(response.getBody().toString(), KakaoGeoRes.class);        //카카오 매핑
        List<Documents> result = new ArrayList<>();     //어레이 리스트로 도큐먼츠만 파싱
        result.addAll(bodyJson.getDocuments());

        log.info("중심좌표 호텔 20키로 값 : "+result.toString());

        return result;
    }

    public List<Places> placeAdd(List<Places> places, int number) {
        //테스트용 경복궁 좌표 : 37.5776087830657 126.976896737645
/*
        List<Places> places = mapRepository.findByAddressNameContains("금천구", Sort.by(Sort.Order.desc("popularity")));
        //매개변수로 받아온 Places로 가정
        //좌표만 받을지 List를 다 받을지 회의 필요?*/
        for (Places p : places){
            log.info(String.valueOf(p));
        }
        /*String x1 = "126.976896737645";  //매개변수로 받을거
        String y1 = "37.5776087830657";  //매개변수로 받을거(제일 위쪽 좌표)
        String x2 = "126.976896737645";  //매개변수로 받을거
        String y2 = "37.5776087830657";  //매개변수로 받을거(제일 아래쪽 좌표)
        String x3 = "126.976896737645";  //매개변수로 받을거
        String y3 = "37.5776087830657";  //매개변수로 받을거(제일 왼쪽 좌표)
        String x4 = "126.976896737645";  //매개변수로 받을거
        String y4 = "37.5776087830657";  //매개변수로 받을거(제일 오른쪽 좌표)*/
        double x_max = Double.parseDouble(places.get(0).getX());
        double x_min = Double.parseDouble(places.get(0).getX());
        double y_max = Double.parseDouble(places.get(0).getY());
        double y_min = Double.parseDouble(places.get(0).getY());
        for (Places p : places){
            if (x_max < Double.parseDouble(p.getX())){
                x_max = Double.parseDouble(p.getX());
            }if (x_min > Double.parseDouble(p.getX())){
                x_min = Double.parseDouble(p.getX());
            }if (y_max < Double.parseDouble(p.getY())){
                y_max = Double.parseDouble(p.getY());
            }if (y_min > Double.parseDouble(p.getY())){
                y_min = Double.parseDouble(p.getY());
            }
        }

        String x_min_s = String.valueOf(x_min).substring(0,7);
        String x_max_s = String.valueOf(x_max).substring(0,7);
        String y_min_s = String.valueOf(y_min).substring(0,6);
        String y_max_s = String.valueOf(y_max).substring(0,6);

        /*x3 = x3.substring(0,7);  //최소 x   중심을 기준으로 검색할때,,
        x4 = x4.substring(0,7);  //최대 x
        y1 = y1.substring(0,6);  //최대 y
        y2 = y2.substring(0,6);  //최소 y
        BigDecimal dx = new BigDecimal(x1);
        BigDecimal dy = new BigDecimal(y1);
        BigDecimal distance = new BigDecimal(String.valueOf(0.01));

        String x_max = String.valueOf(dx.add(distance));
        String x_min = String.valueOf(dx.subtract(distance));
        String y_max = String.valueOf(dy.add(distance));
        String y_min = String.valueOf(dy.subtract(distance));
        log.info(x_max+" "+x_min+" "+y_max+" "+y_min);
        List<Places> places = mapRepository.findByXBetweenAndYBetween(x_min,x_max,y_min,y_max);
        //List<Places> places = mapRepository.findByXBetween(x_min,x_max);
        log.info("경복궁 중심으로 한변이 "+distance.multiply(BigDecimal.valueOf(200)) +
                "km인 정사각형 범위로 불러온 장소 갯수 : " + places.size());*/
        //List<Places> extraPlaces = mapRepository.findByXBetweenAndYBetween(x3,x4,y2,y1);
        List<Places> extraPlaces = mapRepository.findByXBetweenAndYBetween(x_min_s,x_max_s,y_min_s,y_max_s);
        for (Places p : extraPlaces){
            log.info(String.valueOf(p));
            if (!places.contains(p)){
                places.add(p);
            }
        }

        places.addAll(extraPlaces);

        return places;
    }

/*    public void Save_Places(KakaoGeoRes bodyJson[]) {
//        log.info("세이브 메소드에서 받은 bodyJson id값 : " + bodyJson[0].getDocuments().get(0).getId());

        Places[] places = new Places[15];
        for (int i = 0; i < 15; i++) {
            places[i] = new Places();
        }
//        places[0].setPlace_id(bodyJson[0].getDocuments().get(0).getId());
//        log.info("Places배열 정상 작동"+places[0].getPlace_id());

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 15; j++) {
                String category_depth = splitCategory(bodyJson[i].getDocuments().get(j).getCategory_name());
                places[j].setPlace_id(bodyJson[i].getDocuments().get(j).getId());
                places[j].setPlace_name(bodyJson[i].getDocuments().get(j).getPlace_name());
                places[j].setAddress_name(bodyJson[i].getDocuments().get(j).getAddress_name());
                places[j].setPhone(bodyJson[i].getDocuments().get(j).getPhone());
                places[j].setUrl(bodyJson[i].getDocuments().get(j).getPlace_url());
                places[j].setX(bodyJson[i].getDocuments().get(j).getX());
                places[j].setY(bodyJson[i].getDocuments().get(j).getY());
                places[j].setCategory_name(category_depth);
                places[j].setIndoor("0");
                mapRepository.save(places[j]);
            }
        }
//        log.info("for문 적상 작동");
//        log.info("맵 리포지토리 정상 작동");
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ResponseBody
    public void MapParsing2() throws UnirestException, JsonProcessingException {
        *//*대한민국
        최북단 : 강원도 고성군 현내면 대강리 *38.61503025970676 128.3580969588429
        최남단 : 제주특별자치도 서귀포시 대정읍 마라리 *33.11260291103729 126.26834665821991
        최동단 : 경상북도 울릉군 울릉읍 독도리 동도 37.23981774230282 *131.8727302252229945
        최서단 : 인천광역시 옹진군 백령면 연화리. 37.96878377086497 *124.60976081654518
        육지 최동단 : 포항 어딘가 위도 36.01670936347706, 경도 129.5845724459377

        최븍서(좌상단 꼭지점) : 38.61503025970676 124.60976081654518
        최남동(우하단 꼭지점) : 33.11260291103729 131.8727302252229945
        육지 최남동(우하단 꼭지점) : 33.11260291103729 129.5845724459377

        테스트용 경복궁 좌표 : 37.57740 126.97679
        *//*
        String APIKey = "KakaoAK";
//        ArrayList<String> result = new ArrayList<>();

        double distance = 0.01000; //1000m
        double farEast = 131.8727302252229945;
        double farSouth = 35.61503025970676;
        double farWest = 124.60976081654518;
        double farNorth = 36.70503025970684;    //바로 시작하면 됨        //37.11503025970676 부터 함        //38.61503025970676 ~ 38.415030259706796 함
        double y = farNorth;

        while (y > farSouth) {

            double x = farWest;

            while (x < farEast) {
                for(int i=1; i<4; i++){
                    String apiURL = "https://dapi.kakao.com/v2/local/search/category.json?" +
                            "category_group_code=AT4&" +
                            "rect=" + x + "," + y + "," + (x + distance) + "," + (y - distance) + "&" +
                            "page="+i;

                    HttpResponse<JsonNode> response = Unirest
                            .get(apiURL)
                            .header("Authorization", APIKey).asJson();

                    ObjectMapper objectMapper =new ObjectMapper();
                    objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

                    JSONObject jsonObject = response.getBody().getObject();
                    JSONObject meta = (JSONObject) jsonObject.get("meta");


//                    System.out.println("리스펀스 값 : "+response.getBody().toString());



                    if (meta.get("total_count") == "0"){      //검색결과 없으면 탈출
                        break;
                    } else {                                    //아니면 저장
                        KakaoGeoRes bodyJson = objectMapper.readValue(response.getBody().toString(), KakaoGeoRes.class);
                        Save_Places2(bodyJson);
                    }

                    if ((boolean) meta.get("is_end")) {               //마지막페이지면 탈출
                        break;
                    }
                }
                x += distance;          //x값 플러스
            }

            System.out.println("중간 Y값 : "+y);

            y -= distance;              //y값 마이너스
        }
    }

    public String splitCategory(String category){
        if(category.length()!=10){  //3번째 이상 카테고리 있을 경우
            return category.substring(13);
        }
        else{  //2번째 카테고리까지 있는 경우
            return "";
        }
    }
    public void Save_Places2(KakaoGeoRes bodyJson) {
//        log.info("세이브 메소드에서 받은 bodyJson id값 : " + bodyJson[0].getDocuments().get(0).getId());
        int size = bodyJson.getDocuments().size();  //반복문 속도 개선 - 변수 지정하기
        Places[] places = new Places[size];
        for (int i = 0; i < size; i++) {
            places[i] = new Places();
        }
//        places[0].setPlace_id(bodyJson[0].getDocuments().get(0).getId());
//        log.info("Places배열 정상 작동"+places[0].getPlace_id());


        for (int j = 0; j < size; j++) {
//            log.info("세이브플레이스2 바디제이슨 카테고리 값  : " +bodyJson.getDocuments().get(j).getCategory_name());
            String category_depth = splitCategory(bodyJson.getDocuments().get(j).getCategory_name());
            places[j].setPlace_name(bodyJson.getDocuments().get(j).getPlace_name());
            places[j].setAddress_name(bodyJson.getDocuments().get(j).getAddress_name());
            places[j].setUrl(bodyJson.getDocuments().get(j).getPlace_url());
            places[j].setX(bodyJson.getDocuments().get(j).getX());
            places[j].setY(bodyJson.getDocuments().get(j).getY());
            places[j].setPhone(bodyJson.getDocuments().get(j).getPhone());
            places[j].setPlace_id(bodyJson.getDocuments().get(j).getId());
            places[j].setCategory_name(category_depth);
            places[j].setIndoor("0");
            mapRepository.save(places[j]);
        }
//        log.ifo("for문 적상 작동");
//        log.info("맵 리포지토리 정상 작동");
    }*/
}