import org.specs2.mutable.Specification
import services.AdsService

/**
 * Specs for AdsServiceSpec
 */
class AdsServiceSpec extends Specification {

  "AdsService#parseAds" should {
    "get all unique ads urls" in {
      val s =
        """
          <a href="/ventes_immobilieres/698697514.htm?ca=12_s" title="Tr&egrave;s Rare, Maison neuve F3 en beton cellulaire">
          <a href="/ventes_immobilieres/616284831.htm?ca=12_s" title="Maison 7 pi&egrave;ces 170m2">
          <a href="/ventes_immobilieres/698697514.htm?ca=12_s" title="Tr&egrave;s Rare, Maison neuve F3 en beton cellulaire">
        """

      AdsService.parseAds(s) must beEqualTo(
        Set(
          "/ventes_immobilieres/698697514.htm",
          "/ventes_immobilieres/616284831.htm"
        )
      )

    }
  }



}
