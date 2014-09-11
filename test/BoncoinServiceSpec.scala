import org.specs2.mutable.Specification
import services.BoncoinService

/**
 * Specs for BoncoinService
 */
class BoncoinServiceSpec extends Specification {

  "BoncoinService#parseAds" should {
    "get all unique ads urls" in {
      val s =
        """
          <a href="http://www.leboncoin.fr/ventes_immobilieres/698697514.htm?ca=12_s" title="Tr&egrave;s Rare, Maison neuve F3 en beton cellulaire">
          <a href="http://www.leboncoin.fr/ventes_immobilieres/616284831.htm?ca=12_s" title="Maison 7 pi&egrave;ces 170m2">
          <a href="http://www.leboncoin.fr/ventes_immobilieres/698697514.htm?ca=12_s" title="Tr&egrave;s Rare, Maison neuve F3 en beton cellulaire">
        """

      BoncoinService.parseAds(s) must beEqualTo(
        Set(
          "http://www.leboncoin.fr/ventes_immobilieres/698697514.htm",
          "http://www.leboncoin.fr/ventes_immobilieres/616284831.htm"
        )
      )


    }
  }

}
